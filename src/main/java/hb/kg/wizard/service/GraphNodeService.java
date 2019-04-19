package hb.kg.wizard.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.encrypt.MD5Util;
import hb.kg.common.util.http.HttpClientUtil;
import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.common.util.time.TimeUtil;
import hb.kg.law.bean.http.HBLawBasic;
import hb.kg.law.dao.LawDao;
import hb.kg.wizard.bean.enums.HBGraphLinkType;
import hb.kg.wizard.bean.enums.HBGraphNodeType;
import hb.kg.wizard.bean.http.GraphWordHTTP;
import hb.kg.wizard.bean.http.HBGraphWordWithLink;
import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import hb.kg.wizard.bean.mongo.HBGraphLaw;
import hb.kg.wizard.bean.mongo.HBGraphLink;
import hb.kg.wizard.dao.GraphLinkDao;
import hb.kg.wizard.dao.GraphNodeDao;

// 通用的节点服务
@Service
public class GraphNodeService extends BaseCRUDService<HBGraphBaseNode> {
    @Resource
    private GraphNodeDao graphNodeDao;
    @Resource
    private GraphLinkDao graphLinkDao;
    @Resource
    private LawDao lawDao;

    public BaseMongoDao<HBGraphBaseNode> dao() {
        return graphNodeDao;
    }

    /**
     * 按照id号列表查询节点，和该节点相关的所有边，以及这些边对应的节点
     */
    public HBGraphWordWithLink getNodeAndLinkByIdList(List<String> ids) {
        // 先获取所有的边
        Collection<HBGraphLink> links = graphLinkDao.findAllLinksByNodes(ids,
                                                                         HBGraphLinkType.ATTRIBUTE.getName(),
                                                                         HBGraphLinkType.TYPE.getName());
        // 提取所有边对应的所有节点
        HashSet<String> nameset = new HashSet<>();
        nameset.addAll(ids);
        if (CollectionUtils.isNotEmpty(links)) {
            links.stream().forEach(link -> {
                nameset.add(link.getStart());
                nameset.add(link.getEnd());
            });
        }
        // 统一获取这些节点
        Collection<HBGraphBaseNode> words = graphNodeDao.findAllByType(nameset,
                                                                       HBGraphNodeType.LAW.getName());
        return new HBGraphWordWithLink(words, links);
    }

    // 读写锁
    private ReentrantReadWriteLock restoreRobotDictionaryLock = new ReentrantReadWriteLock();
    // 上传进度
    private AtomicInteger RestoreInProgress = new AtomicInteger(100);

    public int getRestoreInProgress() {
        return RestoreInProgress.get();
    }

    /**
     * 将法规导入知识图谱内，clearall为是否需要清除之前的所有数据，如果查询的时候没有返回法规，则任何数据都不做清除
     */
    public String loadLawDataIntoGraph(boolean clearAll) {
        StringBuilder result = new StringBuilder();
        CloseableIterator<HBLawBasic> itr = mongoTemplate.stream(Query.query(Criteria.where("state")
                                                                                     .is(true)),
                                                                 HBLawBasic.class);
        try {
            do {
                if (clearAll) {
                    mongoTemplate.remove(new Query(Criteria.where("type")
                                                           .is(HBGraphNodeType.LAW.getName())),
                                         HBGraphLaw.class);
                    mongoTemplate.remove(new Query(Criteria.where("type")
                                                           .in(HBGraphLinkType.ATTRIBUTE.getName(),
                                                               HBGraphLinkType.TYPE.getName())),
                                         HBGraphLink.class);
                }
                int insertSize = 1000;
                HashMap<String, HBGraphLaw> insertLawMap = new HashMap<>(insertSize * 2); // 每次插入1000条，但考虑到0.75的扩展问题
                HashMap<String, HBGraphLink> insertLinks = new HashMap<>(insertSize * 10);
                while (itr.hasNext()) {
                    HBLawBasic law = itr.next();
                    // 1、先转存节点
                    lawToGraph(law, insertLawMap, insertLinks);
                    if (insertLawMap.size() > insertSize) {
                        if (!clearAll) {
                            // 如果没有做过按照key进行清空，这里要进行清空
                            mongoTemplate.remove(Query.query(Criteria.where("_id")
                                                                     .in(insertLawMap.keySet())),
                                                 HBGraphLaw.class);
                            mongoTemplate.remove(Query.query(Criteria.where("encrypt")
                                                                     .in(insertLinks.keySet())),
                                                 HBGraphLink.class);
                        }
                        // 但是都需要插入
                        mongoTemplate.insertAll(insertLawMap.values());
                        mongoTemplate.insertAll(insertLinks.values());
                        // 最后清空缓存
                        insertLawMap.clear();
                        insertLinks.clear();
                    }
                }
                if (MapUtils.isNotEmpty(insertLawMap)) {
                    if (!clearAll) {
                        // 如果没有做过按照key进行清空，这里要进行清空
                        mongoTemplate.remove(Query.query(Criteria.where("_id")
                                                                 .in(insertLawMap.keySet())),
                                             HBGraphLaw.class);
                        mongoTemplate.remove(Query.query(Criteria.where("encrypt")
                                                                 .in(insertLinks.keySet())),
                                             HBGraphLink.class);
                    }
                    // 但是都需要插入
                    mongoTemplate.insertAll(insertLawMap.values());
                    mongoTemplate.insertAll(insertLinks.values());
                }
                result.append("转存法规信息成功");
            } while (false);
        } catch (Exception e) {
            logger.error("转存法规信息失败", e);
            result.append("转存法规信息失败");
        } finally {
            itr.close();
        }
        return result.toString();
    }

    public void lawToGraph(HBLawBasic law,
                           HashMap<String, HBGraphLaw> insertLawMap,
                           HashMap<String, HBGraphLink> insertLinks) {
        String date;
        if (law.getDate() != null) {
            date = TimeUtil.getStringFromFreq(law.getDate(), "day");
        } else {
            date = "未找到相关信息";
        }
        // 实体id
        HBGraphLaw graphLawId = HBGraphLaw.genGraphLaw(law, "lawId");
        graphLawId.setWord(law.getId());
        insertLawMap.put(graphLawId.getId(), graphLawId);
        // 实体标题
        HBGraphLaw graphLawName = HBGraphLaw.genGraphLaw(law, "name");
        graphLawName.setWord(law.getName());
        insertLawMap.put(graphLawName.getWord(), graphLawName);
        // 实体法规号
        HBGraphLaw graphLawNo = HBGraphLaw.genGraphLaw(law, "no");
        graphLawNo.setWord(law.getNo());
        insertLawMap.put(graphLawNo.getWord(), graphLawNo);
        // 实体发文时间
        HBGraphLaw graphLawDate = HBGraphLaw.genGraphLaw(law, "date");
        graphLawDate.setWord(date);
        insertLawMap.put(graphLawDate.getWord(), graphLawDate);
        // 实体类别
        HBGraphLaw graphLawExcelType = HBGraphLaw.genGraphLaw(law, "excelType");
        graphLawExcelType.setWord(law.getExcelType());
        insertLawMap.put(graphLawExcelType.getWord(), graphLawExcelType);
        // id与标题联系
        HBGraphLink lawLinkName = new HBGraphLink();
        lawLinkName.setType(HBGraphLinkType.ATTRIBUTE.getName());
        lawLinkName.setStart(law.getId());
        lawLinkName.setEnd(law.getName());
        lawLinkName.setNature("name");
        lawLinkName.prepareHBBean();
        insertLinks.put(lawLinkName.getEncrypt(), lawLinkName);
        // id与法规号联系
        HBGraphLink lawLinkNo = new HBGraphLink();
        lawLinkNo.setType(HBGraphLinkType.ATTRIBUTE.getName());
        lawLinkNo.setStart(law.getId());
        lawLinkNo.setEnd(law.getNo());
        lawLinkName.setNature("no");
        lawLinkNo.prepareHBBean();
        insertLinks.put(lawLinkNo.getEncrypt(), lawLinkNo);
        // id与发文时间联系
        HBGraphLink lawLinkDate = new HBGraphLink();
        lawLinkDate.setType(HBGraphLinkType.ATTRIBUTE.getName());
        lawLinkDate.setStart(law.getId());
        lawLinkDate.setEnd(date);
        lawLinkName.setNature("date");
        lawLinkDate.prepareHBBean();
        insertLinks.put(lawLinkDate.getEncrypt(), lawLinkDate);
        // id与分类联系
        HBGraphLink lawLinkExcelType = new HBGraphLink();
        lawLinkExcelType.setType(HBGraphLinkType.TYPE.getName());
        lawLinkExcelType.setStart(law.getId());
        lawLinkExcelType.setEnd(law.getExcelType());
        lawLinkName.setNature("excelType");
        lawLinkExcelType.prepareHBBean();
        insertLinks.put(lawLinkExcelType.getEncrypt(), lawLinkExcelType);
    }

    /**
     * 获取百度API访问token 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     *         "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public String getAuth() {
        // 官网获取的 API Key 更新为你注册的（fmy的key）
        String clientId = "EG9G4HWF6AvzI0S2nOMlUYIF";
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = "SCd8VniInGx1peb66qsnFuglsRh0bNkM";
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + clientId
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + clientSecret;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            JSONObject jsStr = JSONObject.parseObject(result);
            String access_token = jsStr.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * 调用百度nlp对词库中的不存在词性的关键词进行标注， 调用时候请注意 1，查询条件，目前查询nature为空
     * 2，每秒调用次数，现在最高每秒可调用100次，每秒最高调用100次到期时间是2019/04/11日。3，到期后会恢复每秒最多调用5次默认值（可重新申请）
     */
    public void addNature() {
        String accessToken = getAuth();// 获取调取api的钥匙
        if (HBStringUtil.isNotBlank(accessToken)) {
            int success = 0; // 操作成功数
            int not = 0; // 百度为找到词性数
            int failed = 0;// 失败数
            String wordPosUrl = "https://aip.baidubce.com/rpc/2.0/nlp/v1/lexer?access_token="
                    + accessToken;
            Query query = new Query();
            query.addCriteria(Criteria.where("nature").exists(false));
            Long count = mongoTemplate.count(query, GraphWordHTTP.class);
            query.limit(1000);
            CloseableIterator<GraphWordHTTP> itr = null;
            for (int i = 0; i < (count / 1000) + 1; i++) {// 处理游标超时，每次处理1k条数据
                itr = mongoTemplate.stream(query, GraphWordHTTP.class);
                int s = 0;
                while (itr.hasNext()) {
                    GraphWordHTTP word = itr.next();
                    JSONObject jsOb = new JSONObject();
                    try {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("text", word.getWord());// text需要标注的关键词
                        jsOb = new JSONObject(map);
                        String params = jsOb.toJSONString();
                        String httpPostMethod = "";
                        if (s >= 90) {// 当处理90条的时候，线程睡眠1秒
                            try {
                                Thread.sleep(1000);
                                s = 0;
                            } catch (InterruptedException e) {
                                logger.error("睡眠异常", e);
                            }
                        }
                        // API词处理返回结果
                        httpPostMethod = HttpClientUtil.httpPostMethod(wordPosUrl, params);
                        s++;
                        jsOb = JSONObject.parseObject(httpPostMethod);
                        JSONArray jsAr = jsOb.getJSONArray("items");
                        // 对不存在词性的关键词先标注为no
                        mongoTemplate.updateFirst(Query.query(Criteria.where("id")
                                                                      .is(word.getId())),
                                                  new Update().set("nature", "no"),
                                                  GraphWordHTTP.class);
                        boolean k = false;// 记录百度nlp是否有此关键词
                        for (Iterator<Object> iter = jsAr.iterator(); iter.hasNext();) {
                            JSONObject o = (JSONObject) iter.next();
                            if (o.getString("item").equals(word.getWord())
                                    && HBStringUtil.isNotBlank(o.getString("pos"))) {
                                mongoTemplate.updateFirst(Query.query(Criteria.where("id")
                                                                              .is(word.getId())),
                                                          new Update().set("nature",
                                                                           o.getString("pos")),
                                                          GraphWordHTTP.class);
                                logger.info(word.getId() + "->>" + o.getString("pos"));
                                success++;
                                k = true;
                            }
                        }
                        if (!k) {
                            not++;// 未找到记录
                        }
                    } catch (Exception e) {
                        logger.error("更新" + word.getWord() + "失败", e);
                        failed++;
                    }
                }
                itr.close();
            }
            logger.info("成功数：" + success + "失败数：" + failed + "api为找到数" + not);
        }
    }
}
