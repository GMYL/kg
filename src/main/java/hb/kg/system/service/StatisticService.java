//package hb.kg.system.service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//import org.apache.commons.collections.MapUtils;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.Sort.Direction;
//import org.springframework.data.domain.Sort.Order;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.util.CloseableIterator;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import hb.kg.common.bean.job.QueueJob;
//import hb.kg.common.server.QueueServer;
//import hb.kg.common.service.FullBaseCRUDService;
//import hb.kg.common.service.TimerService;
//import hb.kg.common.util.set.HBCollectionUtil;
//import hb.kg.common.util.time.TimeUtil;
//import hb.kg.content.bean.mongo.HBArticle;
//import hb.kg.law.bean.mongo.HBLaw;
//import hb.kg.search.bean.mongo.HBQuestionStandard2;
//import hb.kg.search.service.DictionaryWordService;
//import hb.kg.system.bean.enums.HBStatisticType;
//import hb.kg.system.bean.mongo.HBStatistic;
//import hb.kg.system.bean.report.ArticleDistReport;
//import hb.kg.system.bean.report.GraphDistReport;
//import hb.kg.system.bean.report.LawDistReport;
//import hb.kg.system.bean.report.QaDistReport;
//import hb.kg.system.bean.report.WorkflowDistReport;
//import hb.kg.wizard.bean.mongo.HBGraphLink;
//import hb.kg.wizard.bean.mongo.HBGraphWord;
//import hb.kg.workflow.base.bean.enums.HBWorkflowStateEnum;
//import hb.kg.workflow.base.bean.mongo.WorkflowState;
//
///**
// * 统计数据服务
// */
//@Service
//public class StatisticService extends FullBaseCRUDService<HBStatistic> {
//    @Autowired
//    private QueueServer queueServer;
//    @Autowired
//    private DictionaryWordService dictionaryWordService;
//    private ReentrantReadWriteLock generateLock = new ReentrantReadWriteLock();
//
//    /**
//     * 我们默认报告是天级的，所以每5分钟检查一次所有报告是否有生成，如果没有生成那么对它进行生成
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    public void scheduleGenerateStatistic() {
//        if (sysConfService.getSwitchOnlineReport()) {
//            for (HBStatisticType type : HBStatisticType.values()) {
//                if (type.isAutoGenerate()) {
//                    // 使用异步队列的方式执行
//                    try {
//                        QueueJob queueJob = new QueueJob(type,
//                                                         StatisticService.class.getMethod("doGenerateStatistic",
//                                                                                          HBStatisticType.class),
//                                                         this);
//                        queueServer.addAJob(queueJob);
//                    } catch (Exception e) {
//                        logger.error("添加异步队列的数据出错", e);
//                    }
//                }
//            }
//        }
//    }
//
//    public boolean doGenerateStatistic(String type) {
//        return doGenerateStatistic(HBStatisticType.valueOf(type));
//    }
//
//    /**
//     * 供ALL配置项使用，在生成所有可以生成的报告，但只生成一次
//     */
//    private void doGenerateAllReport() {
//        for (HBStatisticType type : HBStatisticType.values()) {
//            if (type.isAutoGenerate()) {
//                doGenerateStatistic(type);
//            }
//        }
//    }
//
//    public boolean doGenerateStatistic(HBStatisticType type) {
//        try {
//            // 避免同一时间生成的报告过多，但不阻塞生成线程，15秒来源是保证能够完成10次mongo数据库查询，如果这还小于那就是有问题了
//            if (generateLock.writeLock().tryLock(15, TimeUnit.SECONDS)) {
//                try {
//                    if (type != null) {
//                        switch (type) {
//                        case QADIST:
//                            doGenerateQaDist();
//                            break;
//                        case LAWDIST:
//                            doGenerateLawDist();
//                            break;
//                        case ARTICLEDIST:
//                            doGenerateArticleDist();
//                            break;
//                        case GRAPHDIST:
//                            doGenerateGraphDist();
//                            break;
//                        case WORKFLOWDIST:
//                            doGenerateWorkflowDist();
//                            break;
//                        case ALL:
//                            generateLock.writeLock().unlock();
//                            doGenerateAllReport();
//                            break;
//                        default:
//                            break;
//                        }
//                    }
//                    logger.debug("生成报告[" + type + "]成功");
//                    return true;
//                } catch (Exception e) {
//                    logger.error("生成报告[" + type + "]失败", e);
//                    return false;
//                } finally {
//                    if (generateLock.isWriteLockedByCurrentThread()) {
//                        generateLock.writeLock().unlock();
//                    }
//                }
//            } else {
//                logger.warn("已经有其它线程正在生成报告");
//                return true;
//            }
//        } catch (Exception e) {
//            logger.error("生成报告[" + type + "]获得锁的过程中出现了问题", e);
//            return false;
//        }
//    }
//
//    /**
//     * 生成问答库的数据分布
//     */
//    public void doGenerateQaDist() {
//        // 只有在当天的报告没有生成的时候才会生成
//        HBStatistic statistic = getTodayData(HBStatisticType.QADIST.getName());
//        if (statistic == null) {
//            Map<String, Integer> timeMap = generateANewCountMap(); // 统计问题的时间分布
//            Map<String, Integer> visitMap = generateANewCountMap(); // 统计问题的访问次数分布
//            Map<String, Integer> typeMap = generateANewCountMap(); // 统计问题的类型分布
//            Map<String, Integer> darkTitleMap = generateANewCountMap(); // 统计问题的数量分布
//            CloseableIterator<HBQuestionStandard2> itr = mongoTemplate.stream(Query.query(Criteria.where("valid")
//                                                                                                  .is(true)),
//                                                                              HBQuestionStandard2.class);
//            try {
//                while (itr.hasNext()) {
//                    HBQuestionStandard2 questionStandard = itr.next();
//                    // 统计时间分布，精确到天
//                    addToMap(timeMap,
//                             questionStandard.getCreateTime() != null
//                                     ? TimeUtil.getStringFromFreq(questionStandard.getCreateTime(),
//                                                                  "day")
//                                     : null);
//                    // 统计访问次数分布，个位到十位精确到个位，十位到百位精确到百位，百位以上不再进行详细统计
//                    int visitTime = questionStandard.getVisitNum() != null
//                            ? questionStandard.getVisitNum()
//                            : 0;
//                    if (visitTime > 100) {
//                        visitTime = 100;
//                    } else if (visitTime > 10) {
//                        visitTime = visitTime - visitTime % 10;
//                    }
//                    addToMap(visitMap, visitTime + "");
//                    // 统计类型分布
//                    addToMap(typeMap, questionStandard.getType());
//                    // 统计阴标题的数量
//                    addToMap(darkTitleMap, questionStandard.getMapDarkTitle().size() + "");
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr.close();
//            }
//            statistic = new HBStatistic();
//            QaDistReport report = new QaDistReport();
//            report.setTimeMap(timeMap);
//            report.setVisitMap(visitMap);
//            report.setTypeMap(typeMap);
//            report.setDarkTitleMap(darkTitleMap);
//            statistic.setReport(report);
//            statistic.setType(HBStatisticType.QADIST.getName());
//            statistic.prepareHBBean();
//            mongoTemplate.save(statistic); // 万一保存的时候当天的数据已经存在了，那么直接覆盖
//        }
//    }
//
//    /**
//     * 生成法规库的数据分布
//     */
//    public void doGenerateLawDist() {
//        // 只有在当天的报告没有生成的时候才会生成
//        HBStatistic statistic = getTodayData(HBStatisticType.LAWDIST.getName());
//        if (statistic == null) {
//            Map<String, Integer> timeMap = generateANewCountMap();
//            Map<String, Integer> visitMap = generateANewCountMap();
//            Map<String, Integer> typeMap = generateANewCountMap();
//            Map<String, Integer> validMap = generateANewCountMap();
//            Map<String, Integer> attachMap = generateANewCountMap();
//            Map<String, Integer> annexesMap = generateANewCountMap();
//            Map<String, Integer> contentTokMap = generateANewCountMap();
//            CloseableIterator<HBLaw> itr = mongoTemplate.stream(Query.query(Criteria.where("state")
//                                                                                    .is(true)),
//                                                                HBLaw.class);
//            try {
//                while (itr.hasNext()) {
//                    HBLaw bean = itr.next();
//                    // 统计时间分布，精确到月
//                    addToMap(timeMap,
//                             bean.getDate() != null
//                                     ? TimeUtil.getStringFromFreq(bean.getDate(), "month")
//                                     : null);
//                    // 统计访问次数分布，个位到十位精确到个位，十位到百位精确到百位，百位以上不再进行详细统计
//                    int visitTime = bean.getVisitNum() != null ? bean.getVisitNum() : 0;
//                    if (visitTime > 100) {
//                        visitTime = 100;
//                    } else if (visitTime > 10) {
//                        visitTime = visitTime - visitTime % 10;
//                    }
//                    addToMap(visitMap, visitTime + "");
//                    // 统计有效性分布
//                    addToMap(validMap, bean.getValid());
//                    // 统计类型分布
//                    addToMap(typeMap, bean.getExcelType());
//                    // 统计相关法规数量分布
//                    addToMap(attachMap,
//                             "" + (CollectionUtils.isNotEmpty(bean.getAttaches())
//                                     ? bean.getAttaches().size()
//                                     : 0));
//                    // 统计法规的附件分布
//                    addToMap(annexesMap,
//                             "" + (CollectionUtils.isNotEmpty(bean.getAnnexes())
//                                     ? bean.getAnnexes().size()
//                                     : 0));
//                    // 统计法规的高频词分布
//                    Map<String, Integer> wordCountMap = dictionaryWordService.getWordCountMap(bean.getContents());
//                    addToMap(contentTokMap, wordCountMap);
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr.close();
//            }
//            statistic = new HBStatistic();
//            LawDistReport report = new LawDistReport();
//            report.setTimeMap(timeMap);
//            report.setVisitMap(visitMap);
//            report.setTypeMap(typeMap);
//            report.setValidMap(validMap);
//            report.setAttachMap(attachMap);
//            report.setAnnexesMap(annexesMap);
//            // 对词表做一个排序，并截断一下，按照300进行截断
//            report.setContentTokMap(HBCollectionUtil.getMapTopKByValue(contentTokMap, 300, false));
//            statistic.setReport(report);
//            statistic.setType(HBStatisticType.LAWDIST.getName());
//            statistic.prepareHBBean();
//            mongoTemplate.save(statistic); // 万一保存的时候当天的数据已经存在了，那么直接覆盖
//        }
//    }
//
//    /**
//     * 生成文章的统计分布
//     */
//    public void doGenerateArticleDist() {
//        // 只有在当天的报告没有生成的时候才会生成
//        HBStatistic statistic = getTodayData(HBStatisticType.ARTICLEDIST.getName());
//        if (statistic == null) {
//            Map<String, Integer> createMap = generateANewCountMap();
//            Map<String, Integer> publishMap = generateANewCountMap();
//            Map<String, Integer> visitMap = generateANewCountMap();
//            Map<String, Integer> authorMap = generateANewCountMap();
//            Map<String, Integer> stateMap = generateANewCountMap();
//            Map<String, Integer> labelMap = generateANewCountMap();
//            CloseableIterator<HBArticle> itr = mongoTemplate.stream(new Query(), HBArticle.class);
//            try {
//                while (itr.hasNext()) {
//                    HBArticle bean = itr.next();
//                    // 统计上传日期分布，精确到天
//                    addToMap(createMap,
//                             bean.getCreateTime() != null
//                                     ? TimeUtil.getStringFromFreq(bean.getCreateTime(), "day")
//                                     : null);
//                    // 统计发布日期分布，精确到天
//                    addToMap(publishMap,
//                             bean.getPublishTime() != null
//                                     ? TimeUtil.getStringFromFreq(bean.getPublishTime(), "day")
//                                     : null);
//                    // 统计访问次数分布，个位到十位精确到个位，十位到百位精确到百位，百位以上不再进行详细统计
//                    // FIXME 全局点击量都需要变换形式
//                    @SuppressWarnings("deprecation")
//                    int visitTime = bean.getVisitNum() != null ? bean.getVisitNum() : 0;
//                    if (visitTime > 100) {
//                        visitTime = 100;
//                    } else if (visitTime > 10) {
//                        visitTime = visitTime - visitTime % 10;
//                    }
//                    addToMap(visitMap, visitTime + "");
//                    // 统计作者分布
//                    addToMap(authorMap,
//                             bean.getAuthor() != null ? bean.getAuthor().getUserName() : null);
//                    // 统计状态分布
//                    addToMap(stateMap, bean.getState() + "");
//                    // 统计标签分布
//                    addToMap(labelMap,
//                             "" + (CollectionUtils.isNotEmpty(bean.getTaxTag())
//                                     ? bean.getTaxTag().size()
//                                     : 0));
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr.close();
//            }
//            statistic = new HBStatistic();
//            ArticleDistReport report = new ArticleDistReport();
//            report.setCreateMap(createMap);
//            report.setPublishTime(publishMap);
//            report.setAuthorMap(authorMap);
//            report.setStateMap(stateMap);
//            report.setLabelMap(labelMap);
//            statistic.setReport(report);
//            statistic.setType(HBStatisticType.ARTICLEDIST.getName());
//            statistic.prepareHBBean();
//            mongoTemplate.save(statistic); // 万一保存的时候当天的数据已经存在了，那么直接覆盖
//        }
//    }
//
//    /**
//     * 生成知识图谱的统计分布
//     */
//    public void doGenerateGraphDist() {
//        // 只有在当天的报告没有生成的时候才会生成
//        HBStatistic statistic = getTodayData(HBStatisticType.GRAPHDIST.getName());
//        if (statistic == null) {
//            Map<String, Integer> natureMap = generateANewCountMap();
//            Map<String, Integer> nodeWeightMap = generateANewCountMap();
//            Map<String, Integer> linkWeightMap = generateANewCountMap();
//            Map<String, Integer> linkTypeMap = generateANewCountMap();
//            // 首先统计节点
//            CloseableIterator<HBGraphWord> itr = mongoTemplate.stream(new Query(),
//                                                                      HBGraphWord.class);
//            try {
//                while (itr.hasNext()) {
//                    HBGraphWord bean = itr.next();
//                    // 统计词性
//                    addToMap(natureMap, bean.getNature());
//                    // 统计节点权重，保留一位小数，注意小数点不能作为mongo里的key，否则会报错
//                    addToMap(nodeWeightMap, Math.round(bean.getWeight() * 10) + "");
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr.close();
//            }
//            // 统计边
//            CloseableIterator<HBGraphLink> itr2 = mongoTemplate.stream(new Query(),
//                                                                       HBGraphLink.class);
//            try {
//                while (itr2.hasNext()) {
//                    HBGraphLink bean = itr2.next();
//                    // 统计边权重，保留一位小数，注意小数点不能作为mongo里的key，否则会报错
//                    addToMap(linkWeightMap, Math.round(bean.getWeight() * 10) + "");
//                    // 统计边类型
//                    addToMap(linkTypeMap, bean.getType());
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr2.close();
//            }
//            statistic = new HBStatistic();
//            GraphDistReport report = new GraphDistReport();
//            report.setNatureMap(natureMap);
//            report.setNodeWeightMap(nodeWeightMap);
//            report.setLinkWeightMap(linkWeightMap);
//            report.setLinkTypeMap(linkTypeMap);
//            statistic.setReport(report);
//            statistic.setType(HBStatisticType.GRAPHDIST.getName());
//            statistic.prepareHBBean();
//            mongoTemplate.save(statistic); // 万一保存的时候当天的数据已经存在了，那么直接覆盖
//        }
//    }
//
//    /**
//     * 生成工作流的数据统计分布
//     */
//    public void doGenerateWorkflowDist() {
//        // 只有在当天的报告没有生成的时候才会生成
//        HBStatistic statistic = getTodayData(HBStatisticType.WORKFLOWDIST.getName());
//        if (statistic == null) {
//            Map<String, Integer> stateMap = generateANewCountMap();
//            Map<String, Integer> managerMap = generateANewCountMap();
//            // 统计workflow的状态
//            CloseableIterator<WorkflowState> itr = mongoTemplate.stream(new Query(),
//                                                                        WorkflowState.class);
//            try {
//                while (itr.hasNext()) {
//                    WorkflowState bean = itr.next();
//                    // 统计WorkflowState的状态
//                    addToMap(stateMap,
//                             HBWorkflowStateEnum.valueOf(bean.getStatecurrent()).getName());
//                    // 统计负责组
//                    addToMap(managerMap,
//                             bean.getManager() != null ? bean.getManager().getName() : null);
//                }
//            } catch (Exception e) {
//                logger.error("统计过程失败", e);
//            } finally {
//                itr.close();
//            }
//            statistic = new HBStatistic();
//            WorkflowDistReport report = new WorkflowDistReport();
//            report.setStateMap(stateMap);
//            report.setManagerMap(managerMap);
//            statistic.setReport(report);
//            statistic.setType(HBStatisticType.WORKFLOWDIST.getName());
//            statistic.prepareHBBean();
//            mongoTemplate.save(statistic); // 万一保存的时候当天的数据已经存在了，那么直接覆盖
//        }
//    }
//
//    protected String unKnownFieldName = "未知";
//
//    /**
//     * 初始化空的统计用Map，保证里面都有unKnownFieldName这一项
//     */
//    protected Map<String, Integer> generateANewCountMap() {
//        Map<String, Integer> rsMap = new HashMap<>();
//        rsMap.put(unKnownFieldName, 0);
//        return rsMap;
//    }
//
//    /**
//     * 主要针对Map，put一个数据，如果存在+1，不存在就新建
//     */
//    protected void addToMap(Map<String, Integer> map,
//                            String insertKey) {
//        addToMap(map, insertKey, unKnownFieldName);
//    }
//
//    /**
//     * 如果是两个map相加，使用这个方法
//     */
//    protected void addToMap(Map<String, Integer> beInsertMap,
//                            Map<String, Integer> toInsertMap) {
//        if (MapUtils.isNotEmpty(beInsertMap) && MapUtils.isNotEmpty(toInsertMap)) {
//            for (Entry<String, Integer> entry : toInsertMap.entrySet()) {
//                String key = entry.getKey().replaceAll("\\.", ""); // json的key里面不能有点存在
//                Integer value = beInsertMap.get(key);
//                if (value != null) {
//                    beInsertMap.put(key, value + entry.getValue());
//                } else {
//                    beInsertMap.put(key, entry.getValue());
//                }
//            }
//        }
//    }
//
//    protected void addToMap(Map<String, Integer> map,
//                            String insertKey,
//                            String defaultKey) {
//        defaultKey = StringUtils.isNotBlank(defaultKey) ? defaultKey : unKnownFieldName;
//        String key = StringUtils.isNotBlank(insertKey) ? insertKey.replace("\\.", "") : defaultKey; // json的key里面不能有点存在
//        Integer value = map.get(key);
//        if (value != null) {
//            map.put(key, value + 1);
//        } else {
//            map.put(key, 1);
//        }
//    }
//
//    /**
//     * 获取最新的一条
//     */
//    public HBStatistic getNewestData(String type) {
//        Query query = new Query();
//        if (StringUtils.isNotBlank(type)) {
//            query.addCriteria(Criteria.where("type").is(type));
//        }
//        query.with(Sort.by(new Order(Direction.DESC, "createTime")));
//        return mongoTemplate.findOne(query, HBStatistic.class);
//    }
//
//    /**
//     * 获取当天的报告
//     */
//    public HBStatistic getTodayData(String type) {
//        Query query = new Query();
//        if (StringUtils.isNotBlank(type)) {
//            query.addCriteria(Criteria.where("type").is(type));
//        }
//        query.addCriteria(Criteria.where("id")
//                                  .gte(TimerService.now_to_day)
//                                  .lt(TimerService.nxt_to_day));
//        return mongoTemplate.findOne(query, HBStatistic.class);
//    }
//}
