package hb.kg.wizard.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.algo.nlp.util.MaxHeap;
import hb.kg.common.util.encrypt.MD5Util;
import hb.kg.common.util.http.HttpClientUtil;
import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.common.util.time.TimeUtil;
import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.law.bean.mongo.HBLawitem;
import hb.kg.law.dao.LawDao;
import hb.kg.search.bean.mongo.HBDictionaryWord;
import hb.kg.search.service.DictionaryWordService;
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
    @Autowired
    private DictionaryWordService dictionaryWordService;

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
                                                                         HBGraphLinkType.TYPE.getName(),
                                                                         HBGraphLinkType.ITEM.getName());
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

    /**
     * 按照一个节点查找它的临边和对应的点
     */
    public HBGraphWordWithLink getNodeAndLinksById(String id,
                                                   int level) {
        // 先定义最终的边
        HashMap<String, HBGraphLink> linkMap = new HashMap<>();
        HashMap<String, HBGraphBaseNode> nodeMap = new HashMap<>();
        final HashSet<String> nameset = new HashSet<>();
        nameset.add(id);
        for (int i = 0; i < level; i++) {
            Collection<HBGraphLink> links = graphLinkDao.findAllLinksByNodes(nameset);
            if (CollectionUtils.isNotEmpty(links)) {
                links.forEach(link -> {
                    linkMap.put(link.getId(), link);
                });
            }
            if (CollectionUtils.isNotEmpty(links)) {
                links.forEach(link -> {
                    if (!nodeMap.containsKey(link.getStart())) {
                        nameset.add(link.getStart());
                    }
                    if (!nodeMap.containsKey(link.getEnd())) {
                        nameset.add(link.getEnd());
                    }
                });
            }
            // 统一获取这些节点
            Query query = new Query();
            // query.addCriteria(Criteria.where("type").in("word"));
            query.addCriteria(Criteria.where("word").in(nameset));
            Collection<HBGraphLaw> words = mongoTemplate.find(query, HBGraphLaw.class);
            if (CollectionUtils.isNotEmpty(words)) {
                words.forEach(word -> {
                    nodeMap.put(word.getWord(), word);
                });
            }
            nameset.clear();
        }
        return new HBGraphWordWithLink(nodeMap.values(), linkMap.values());
    }

    // 读写锁
    // private ReentrantReadWriteLock restoreRobotDictionaryLock = new
    // ReentrantReadWriteLock();
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
        CloseableIterator<HBLaw> itr = mongoTemplate.stream(Query.query(Criteria.where("state")
                                                                                .is(true)),
                                                            HBLaw.class);
        try {
            do {
                if (clearAll) {
                    mongoTemplate.remove(new Query(), HBGraphLaw.class);
                    mongoTemplate.remove(new Query(), HBGraphLink.class);
                }
                int insertSize = 1000;
                HashMap<String, HBGraphLaw> insertLawMap = new HashMap<>(insertSize * 2); // 每次插入1000条，但考虑到0.75的扩展问题
                HashMap<String, HBGraphLink> insertLinks = new HashMap<>(insertSize * 10);
                while (itr.hasNext()) {
                    HBLaw law = itr.next();
                    // 1、先转存节点
                    lawToGraph(law, insertLawMap, insertLinks);
                    if (insertLawMap.size() > insertSize) {
                        // if (!clearAll) {
                        // 如果没有做过按照key进行清空，这里要进行清空
                        mongoTemplate.remove(Query.query(Criteria.where("word")
                                                                 .in(insertLawMap.keySet())),
                                             HBGraphLaw.class);
                        mongoTemplate.remove(Query.query(Criteria.where("encrypt")
                                                                 .in(insertLinks.keySet())),
                                             HBGraphLink.class);
                        // }
                        // 但是都需要插入
                        mongoTemplate.insertAll(insertLawMap.values());
                        mongoTemplate.insertAll(insertLinks.values());
                        // 最后清空缓存
                        insertLawMap.clear();
                        insertLinks.clear();
                    }
                }
                if (MapUtils.isNotEmpty(insertLawMap)) {
                    // if (!clearAll) {
                    // 如果没有做过按照key进行清空，这里要进行清空
                    mongoTemplate.remove(Query.query(Criteria.where("word")
                                                             .in(insertLawMap.keySet())),
                                         HBGraphLaw.class);
                    mongoTemplate.remove(Query.query(Criteria.where("encrypt")
                                                             .in(insertLinks.keySet())),
                                         HBGraphLink.class);
                    // }
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

    /**
     * 添加itms
     * @param json
     * @param law
     */
    public void jsonLawAddItems(JSONObject json,
                                HBLaw law) {
        List<String> items = new ArrayList<>();
        Criteria c = Criteria.where("id").regex("^" + law.getId() + ".*");
        Query query = new Query(c);
        List<HBLawitem> itemList = mongoTemplate.find(query, HBLawitem.class);
        for (HBLawitem item : itemList) {
            items.add(item.getId());
        }
        json.put("items", items);
    }

    public void lawToGraph(HBLaw law,
                           HashMap<String, HBGraphLaw> insertLawMap,
                           HashMap<String, HBGraphLink> insertLinks) {
        JSONObject json = (JSONObject) JSONObject.toJSON(law);
        jsonLawAddItems(json, law);// 添加items
        for (String str : json.keySet()) {
            if (json.get(str) != null) {
                HBGraphLaw graphLaw = new HBGraphLaw();
                graphLaw = HBGraphLaw.genGraphLaw(law, str);
                HBGraphLink lawLink = new HBGraphLink();
                lawLink.setNature(str);
                switch (str) {
                case "id":// id不建立联系
                    graphLaw.setWord(json.getString(str));
                    break;
                case "items":// 条款关系建立 在这单独走
                    JSONArray itemJSONArray = json.getJSONArray(str);
                    for (int i = 0; i < itemJSONArray.size(); i++) {
                        String item = itemJSONArray.getString(i); // O000100100
                        // 添加节点
                        String itemSub = item.substring(5, 8);// 001
                        if (!"000".equals(itemSub)
                                && item.substring(item.length() - 1, item.length()).equals("0")) {
                            HBGraphLaw graphLawItem = new HBGraphLaw();
                            graphLawItem = HBGraphLaw.genGraphLaw(law, str);
                            HBGraphLink lawLinkItem = new HBGraphLink();
                            graphLawItem.setWord(item);
                            lawLinkItem.setStart(law.getId());
                            lawLinkItem.setEnd(item);
                            lawLinkItem.setNature("条款第" + itemSub + "条");
                            lawLinkItem.setType(HBGraphLinkType.ITEM.getName());
                            lawLinkItem.setEncrypt(new String(MD5Util.EncodeByMd5(law.getId()
                                    + item)));
                            lawLinkItem.prepareHBBean();
                            insertLawMap.put(graphLawItem.getWord(), graphLawItem);
                            insertLinks.put(lawLinkItem.getEncrypt(), lawLinkItem);
                        }
                    }
                    break;
                case "excelType":// 分类
                    graphLaw.setWord(json.getString(str));
                    lawLink.setStart(law.getId());
                    lawLink.setEnd(json.getString(str));
                    lawLink.setType(HBGraphLinkType.TYPE.getName());
                    break;
                case "contents":// 内容
                    graphLaw.setWord(law.getId() + "|内容");
                    lawLink.setStart(law.getId());
                    lawLink.setEnd(law.getId() + "|内容");
                    lawLink.setType(HBGraphLinkType.ATTRIBUTE.getName());
                    break;
                case "attaches":// 相关文件
                    JSONArray attaches = json.getJSONArray(str);
                    for (int i = 0; i < attaches.size(); i++) {
                        String s = attaches.getString(i);
                        HBGraphLink lawLinkAttaches = new HBGraphLink();
                        lawLinkAttaches.setNature(str);
                        lawLinkAttaches.setStart(law.getId());
                        lawLinkAttaches.setEnd(s);
                        lawLinkAttaches.setType(HBGraphLinkType.ATTRIBUTE.getName());
                        lawLinkAttaches.setEncrypt(MD5Util.getRandomMD5Code(law.getId() + s));
                        lawLinkAttaches.prepareHBBean();
                        insertLinks.put(lawLinkAttaches.getEncrypt(), lawLinkAttaches);
                    }
                    break;
                case "date":
                    graphLaw.setWord(TimeUtil.getStringFromFreq(json.getDate(str), "day"));
                    lawLink.setStart(law.getId());
                    lawLink.setEnd(TimeUtil.getStringFromFreq(json.getDate(str), "day"));
                    lawLink.setType(HBGraphLinkType.ATTRIBUTE.getName());
                    break;
                case "state":// 法规有效性，后台判断
                case "links":// 文内链接
                case "linkReplaces":// 文内链接替换项
                case "annexes":// 附件
                case "sequence":// 排序值
                case "pictures":// 图片集
                case "from":// 法规原文
                case "alias":// 别名
                case "timeout"://
                case "asc"://
                case "page"://
                case "sortKey"://
                case "autoSave"://
                case "vn"://
                    break;
                default:
                    graphLaw.setWord(json.getString(str).length() > 150
                            ? json.getString(str).substring(0, 100)
                            : json.getString(str));
                    lawLink.setStart(law.getId());
                    lawLink.setEnd(json.getString(str).length() > 150
                            ? json.getString(str).substring(0, 100)
                            : json.getString(str));
                    lawLink.setType(HBGraphLinkType.ATTRIBUTE.getName());
                    break;
                }
                if (HBStringUtil.isNotBlank(graphLaw.getWord())) {
                    insertLawMap.put(graphLaw.getWord(), graphLaw);
                }
                if (HBStringUtil.isNotBlank(lawLink.getEnd())) {
                    lawLink.prepareHBBean();
                    insertLinks.put(lawLink.getEncrypt(), lawLink);
                }
            }
        }
    }

    public void createFileTxt(List<String> ids) {
        try {
            File writeName = new File("src/main/resources/download/laws.txt"); // 相对路径，如果没有则要建立一个新的.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                    BufferedWriter out = new BufferedWriter(writer)) {
                for (String id : ids) {
                    JSONObject results = getTermAndRank(id, 10);
                    out.write(results.toJSONString());
                    out.write("\r\n"); // \r\n即为换行
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回分数最高的前size个分词结果和对应的rank
     * @param content
     * @param size
     * @return
     */
    public JSONObject getTermAndRank(String lawId,
                                     Integer size) {
        JSONObject results = new JSONObject();
        HBLaw law = lawDao.findOne(lawId);
        if (law != null) {
            Map<String, Float> map = getTermAndRank(law.getContents());
            Map<String, Float> result = new LinkedHashMap<String, Float>();
            // 排序
            for (Map.Entry<String, Float> entry : new MaxHeap<Map.Entry<String, Float>>(size,
                new Comparator<Map.Entry<String, Float>>() {
                    @Override
                    public int compare(Map.Entry<String, Float> o1,
                                       Map.Entry<String, Float> o2) {
                        return o1.getValue()
                                 .compareTo(o2.getValue());
                    }
                }).addAll(map.entrySet())
                  .toList()) {
                result.put(entry.getKey(), entry.getValue());
            }
            results.put("result", result);
            results.put("lawTitle", law.getName());
        } else {
            results.put("msg", "未找到法规");
        }
        return results;
    }

    /**
     * 阻尼系数（ＤａｍｐｉｎｇＦａｃｔｏｒ），一般取值为0.85
     */
    final static float d = 0.85f;
    /**
     * 最大迭代次数
     */
    public static int max_iter = 200;
    final static float min_diff = 0.001f;

    public Map<String, Float> getTermAndRank(String content) {
        List<String> listWord = new ArrayList<>();
        List<String> listContent = dictionaryWordService.getWordList(content);
        // 获取词性
        for (String word : listContent) {
            // 获取词性
            HBDictionaryWord dbWords = mongoTemplate.findOne(Query.query(Criteria.where("word")
                                                                                 .is(word)),
                                                             HBDictionaryWord.class);
            // 去除一些属性词
            if (dbWords != null
                    && dictionaryWordService.natureDel(dbWords.getWord(), dbWords.getNature())) {
                listWord.add(word);
            }
        }
        Map<String, Set<String>> words = new TreeMap<String, Set<String>>();
        Queue<String> que = new LinkedList<String>();
        for (String w : listWord) {
            if (!words.containsKey(w)) {
                words.put(w, new TreeSet<String>());
            }
            // 复杂度O(n-1)
            if (que.size() >= 5) {
                que.poll();
            }
            for (String qWord : que) {
                if (w.equals(qWord)) {
                    continue;
                }
                // 既然是邻居,那么关系是相互的,遍历一遍即可
                words.get(w).add(qWord);
                words.get(qWord).add(w);
            }
            que.offer(w);
        }
        // System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < max_iter; ++i) {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - d);
                for (String element : value) {
                    int size = words.get(element).size();
                    if (key.equals(element) || size == 0)
                        continue;
                    m.put(key,
                          m.get(key) + d / size
                                  * (score.get(element) == null ? 0 : score.get(element)));
                }
                max_diff = Math.max(max_diff,
                                    Math.abs(m.get(key)
                                            - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= min_diff)
                break;
        }
        //
        return score;
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
