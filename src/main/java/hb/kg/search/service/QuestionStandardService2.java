package hb.kg.search.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import com.mongodb.client.AggregateIterable;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.algo.HBMathUtil;
import hb.kg.common.util.algo.nlp.trie.NlpWord;
import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.JSONObject;
import hb.kg.law.dao.LawDao;
import hb.kg.law.dao.LawitemDao;
import hb.kg.search.bean.http.HBDarkTitle;
import hb.kg.search.bean.http.HBQuestionStandardMapTitle;
import hb.kg.search.bean.mongo.HBDictionaryWord;
import hb.kg.search.bean.mongo.HBQuestionStandard2;
import hb.kg.search.dao.mongo.QuestionStandardDao2;

@Service
public class QuestionStandardService2 extends BaseCRUDService<HBQuestionStandard2> {
    @Resource
    private LawitemDao lawitemDao;
    @Resource
    private LawDao lawDao;
//    @Autowired
//    private RobotService robotService;
    @Resource
    private QuestionStandardDao2 questionStandardDao2;

    public BaseDao<HBQuestionStandard2> dao() {
        return questionStandardDao2;
    }

    @Autowired
    private DictionaryWordService dictionaryWordService;

    /**
     * 刷新当前库内的问答，利用当前标题生成暗标题
     * 对tf-idf进行优化，因为标题文本较短，所以去除tf词频统计部分采用（词权-idf）为标题分词进行加权
     */
    public String generateDarkTitle() {
        // 文档总数用于idf计算
        Long count = mongoTemplate.count(new Query(), HBQuestionStandardMapTitle.class);
        int success = 0;
        int failed = 0;
        CloseableIterator<HBQuestionStandardMapTitle> itr = null;
        // 处理mongodb查询游标过期,每次处理10k数据，直到结束
        for (int i = 0; i < (count / 10000) + 1; i++) {
            logger.info("已完成：" + success);
            itr = mongoTemplate.stream(new Query(Criteria.where("title").exists(true)).skip(success
                    + failed).limit(10000), HBQuestionStandardMapTitle.class);
            while (itr.hasNext()) {
                try {
                    HBQuestionStandardMapTitle question = itr.next();
                    int titleLength = question.getTitle().length();// 标题的长度
                    List<HBDarkTitle> listDarkTitle = new ArrayList<>();
                    // 获取标题分词Map<"词", 词出现标题中的位置>
                    Map<String, Integer> mapTitle = dictionaryWordService.getTitleMap(question.getTitle()
                                                                                              .replaceAll("、",
                                                                                                          ""));
                    for (Entry<String, Integer> entry : mapTitle.entrySet()) {
                        HBDarkTitle mapDarkTitle = new HBDarkTitle();
                        // 获取词性
                        HBDictionaryWord dbWords = mongoTemplate.findOne(Query.query(Criteria.where("word")
                                                                                             .is(entry.getKey())),
                                                                         HBDictionaryWord.class);
                        // 计算基础权重entry.getKey()关键词、entry.getValue()关键词位置、
                        // dbWords.getNature()词性、titleLength标题长度、title.size()标题分数数量
                        double wei = getWeight(dbWords,
                                               entry.getValue(),
                                               titleLength,
                                               mapTitle.size());
                        // 基础idf
                        double idf = getIDF(count, dbWords.getOccurNum());
                        // 最终权重=基础权重+此词的idf
                        double weight = wei + idf;
                        // double sigmoid = HBMathUtil.sigmoid10(weight);
                        mapDarkTitle.setWord(entry.getKey());
                        mapDarkTitle.setWeight(HBMathUtil.sigmoid10(weight));
                        listDarkTitle.add(mapDarkTitle);
                    }
                    // 更新
                    mongoTemplate.updateFirst(Query.query(Criteria.where("id")
                                                                  .is(question.getId())),
                                              new Update().set("mapDarkTitle", listDarkTitle),
                                              HBQuestionStandardMapTitle.class);
                    success++;
                } catch (Exception e) {
                    logger.error("生成mapDarkTitle暗标题失败", e);
                    failed++;
                }
            }
            itr.close();
        }
        return "mapDarkTitle生成成功" + success + "个，mapDarkTitle生成失败" + failed + "个";
    }

    /*
     * 基础权重
     */
    private double getWeight(HBDictionaryWord word,
                             Integer offe,
                             int titleLength,
                             int wordsCount) {
        if (word.getWord().trim().length() < 2) {
            return 0;
        }
        Double weight = 0.0;
        if (word.getWeight() != null) {
            weight = word.getWeight();// 获取自定义的权重
        }
        // 根据词性获取一个权重
        Double posScore = natureManuWeight(word.getNature());
        if (posScore == null) {
            posScore = 1.0;
        } else if (posScore == 0) {
            return 0;
        }
        // 根据关键词的位置和标题分词数量计算一个权重
        // 头尾关键词权重稍高，中间部分词权重稍低
        // 如果标题长度大于30对关键词适当降权处理
        if (offe < titleLength / 2) {
            // return (titleLength - offe) * (posScore + weight) / wordsCount;
            if (titleLength < 30) {
                return (titleLength - offe) * (posScore + weight) / wordsCount;
            } else if (titleLength < 45) {
                return ((titleLength - offe) * (posScore + weight) / wordsCount) * 0.66;
            } else {
                return ((titleLength - offe) * (posScore + weight) / wordsCount) * 0.33;
            }
        }
        if (titleLength < 30) {
            return offe * (posScore + weight) / wordsCount;
        } else if (titleLength < 60) {
            return (offe * (posScore + weight) / wordsCount) * 0.66;
        } else {
            return (offe * (posScore + weight) / wordsCount) * 0.33;
        }
    }

    /**
     * idf权重
     * @param occurNum 每个词在所有问题标题中出现的次数
     * @param count 问答总数
     * @return
     */
    public Double getIDF(Long count,
                         Integer occurNum) {
        // 计算包含此关键词的文档数
        Double idf = Math.log(count / (occurNum + 1));
        return idf;
    }

    /**
     * 处理词义，按照不同词义进行提权
     */
    public Double natureManuWeight(String nature) {
        if (StringUtils.isNotBlank(nature)) {
            switch (nature) {
            case "ncs":
                return 9.0; // 关键词
            case "nt":
            case "ns":
            case "nsf":
            case "nz":
                return 3.0; // 机构团体名
            case "nr":
            case "nr1":
            case "nr2":
            case "nrj":
            case "nrf":
                return 3.0; // 人名
            case "n":
            case "nl":
            case "ng":
                return 3.0; // 名词
            case "v":
            case "vd":
            case "vn":
            case "vf":
            case "vx":
            case "vi":
            case "vl":
            case "vg":
                return 0.2; // 动词
            case "a":
            case "ad":
            case "an":
            case "ag":
            case "al":
                return 0.2; // 形容词
            case "r":
            case "rr":
            case "rz":
            case "rzt":
            case "rzs":
            case "rzv":
            case "ry":
            case "ryt":
            case "rys":
            case "ryv":
            case "rg":
                return 0.2; // 代词
            case "q":
            case "qv":
            case "qt":
                return 0.2; // 量词
            case "c":
            case "cc":
                return 0.2; // 连词break;
            case "f":
                return 0.2; // 方位词
            case "l":
                return 0.2; // 习用语
            default:
                break;
            }
        }
        return null;
    }

    /**
     * 对查找出来的term进行topN截断
     */
    private List<NlpWord> cutTermsByTopN(List<NlpWord> words,
                                         int n) {
        if (CollectionUtils.isNotEmpty(words) && words.size() > n) {
            words.sort(new Comparator<NlpWord>() {
                public int compare(NlpWord o1,
                                   NlpWord o2) {
                    return Double.compare(o2.getValue(), o1.getValue());
                }
            });
            return words.subList(0, n);
        } else {
            return words;
        }
    }

    /**
     * mongodb聚合管道查询，利用仿原生语句方法进行进行创建语句
     * @param words
     * @return
     */
    private List<Document> mongoQueryDocument(List<String> words) {
        List<Document> dbObjects = new ArrayList<>();
        Document limit = new Document();
        Document project = new Document();
        Document condition = new Document();
        Document project2 = new Document();
        Document condition2 = new Document();
        Document sort = new Document();
        // 第一次聚合
        condition.put("title", 1);// 返回title
        condition.put("mapDarkTitle", 1);// 返回mapDarkTitle
        List<Object> listIntersects = new ArrayList<>();
        listIntersects.add(words);
        listIntersects.add("$mapDarkTitle.word");
        condition.put("commonToBoth", new Document("$setIntersection", listIntersects));// mapDarkTitle.word与words的交集
        project.put("$project", condition);
        // 第二次聚合
        condition2.put("title", 1);// 返回title
        condition2.put("mapDarkTitle", 1);// 返回mapDarkTitle
        condition2.put("commonToBoth", 1);// 返回commonToBoth交集集合
        condition2.put("commonToBothSize", new Document("$size", "$commonToBoth"));// commonToBoth交集集合的长度
        project2.put("$project", condition2);
        // 根据交集集合的长度进行降序，从高到低
        sort.put("$sort", new Document("commonToBothSize", -1));
        // 截取前50条返回
        limit.put("$limit", 50);
        dbObjects.add(project);
        dbObjects.add(project2);
        dbObjects.add(sort);
        dbObjects.add(limit);
        return dbObjects;
    }

    /**
     * 重写list排序方法,字符串长度降序
     * @param words
     * @return
     */
    private List<String> listSortDESC(Collection<String> words) {
        if (CollectionUtils.isNotEmpty(words)) {
            List<String> listWords = new ArrayList<>(words);
            Collections.sort(listWords, new Comparator<String>() {
                public int compare(String var1,
                                   String var2) {
                    if (var1.length() < var2.length()) {
                        return 1;
                    } else if (var1.length() == var2.length()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            return listWords;
        }
        return null;
    }

    /**
     * 对标题进行高亮处理
     */
    private String highlightQuestionTitles(String title,
                                           Collection<String> words) {
        if (CollectionUtils.isNotEmpty(words)) {
            for (String word : words) {
                if (StringUtils.isNotBlank(word)) {
                    title = title.replaceAll(word, "<em>" + word + "</em>");
                }
            }
        }
        return title;
    }

    /**
     * 降序排序并截取n条数据
     * @param words
     * @param n
     * @return
     */
    private List<Document> cutTerms(List<Document> words,
                                    int n) {
        if (CollectionUtils.isNotEmpty(words) && words.size() > n) {
            words.sort(new Comparator<Document>() {
                public int compare(Document o1,
                                   Document o2) {
                    return Double.compare(o2.getDouble("s"), o1.getDouble("s"));
                }
            });
            return words.subList(0, n);
        } else {
            return words;
        }
    }

    /**
     * mongodb版本问题回答，接口响应时间不及es版
     */
    @SuppressWarnings("unchecked")
    public JSONObject mongoAnswer(String question) {
        JSONObject rsJson = new JSONObject();
        if (question == null || question.length() == 0) {
            rsJson.put("error", "您查询的问题有误，请重新输入");
            return rsJson;
        }
//        Map<String, NlpWord> wordCountMap = robotService.getQuestionTermsBasic(question);
        Map<String, NlpWord> wordCountMap = null;
        dictionaryWordService.supplementSynonymsWords(wordCountMap, question);
        List<NlpWord> srcwords = new ArrayList<>();
        dictionaryWordService.uplowerNatureWeight(wordCountMap, question);
        // 重复出现词降权
        srcwords = wordCountMap.entrySet().stream().map(entry -> {
            // 这里是boost值的计算，当前公式是boost=sigmoid(出现次数)*词的权重，sigmoid参数是1->0.8,oo->1
            NlpWord word = entry.getValue();
            word.setValue(HBMathUtil.sigmoidToFlat(word.getNum(), 0.36171) * word.getWeight());
            return entry.getValue();
        }).collect(Collectors.toList());
        srcwords = cutTermsByTopN(srcwords, 30);// 截取前30个关键词
        List<String> words = new ArrayList<>();
        // 只提取分词
        for (NlpWord w : srcwords) {
            words.add(w.getName());
        }
        if (CollectionUtils.isEmpty(words)) {
            rsJson.put("error", "分词有误");
            return rsJson;
        }
        // 聚合管道查询
        AggregateIterable<Document> re = mongoTemplate.getCollection("hb_question_standards2")
                                                      .aggregate(mongoQueryDocument(words));
        List<Document> listDocument = new ArrayList<>();
        // 对返回结果进行处理
        for (Iterator<Document> it = re.iterator(); it.hasNext();) {
            Document dbo = it.next();
            Double weight = 0.0;
            List<String> commonToBoth = (List<String>) dbo.get("commonToBoth");
            List<Map<String, Object>> mapDarkTitle = (List<Map<String, Object>>) dbo.get("mapDarkTitle");
            Map<String, Double> mapWork = new HashMap<>();
            for (Map<String, Object> dark : mapDarkTitle) {
                String s = "";
                Double d = 0.0;
                for (Map.Entry<String, Object> entry : dark.entrySet()) {
                    if (entry.getKey().equals("word")) {
                        s = (String) entry.getValue();
                    } else {
                        d = (Double) entry.getValue();
                    }
                }
                mapWork.put(s, d);
            }
            for (String word : commonToBoth) {
                weight = mapWork.get(word) + weight;
            }
            if (dbo.get("title").equals(question)) {
                dbo.put("s", weight + 10);
                dbo.put("notes", "此问题是用户询问库中问题，s加十分处理");
            } else {
                dbo.put("s", weight);
                dbo.put("notes", "无");
            }
            listDocument.add(dbo);
        }
        listDocument = cutTerms(listDocument, 11);
        if (listDocument.size() > 0 && listDocument.get(0) != null) {
            // 对第一个答案进行彻底封装
            String id = listDocument.get(0).getString("_id");
            HBQuestionStandard2 questionStandardBean = questionStandardDao2.findOne(id);
            if (questionStandardBean != null) {
                rsJson.put("answer", questionStandardBean);
                if (questionStandardBean.getItems() != null) {
                    // 填充内容
                    questionStandardBean.setLawitems(lawitemDao.getMany(questionStandardBean.getItems()));
                    // 填充法规基本信息
//                    questionStandardBean.setLawitemBelongs(lawDao.getManyBasicEver(questionStandardBean.getItems()));
                }
            }
            // 添加分数
            questionStandardBean.setScore(listDocument.get(0).getDouble("s"));
            questionStandardBean.setNotes(listDocument.get(0).getString("notes"));// 说明
        }
        if (listDocument.size() > 1) {
            // 对其他答案仅仅列出标题
            JSONArray hits = new JSONArray();
            List<String> listWords = listSortDESC(words);
            for (int i = 1; i < listDocument.size(); i++) {
                JSONObject hitJson = new JSONObject();
                hitJson.put("id", listDocument.get(i).getString("_id"));
                HBQuestionStandard2 questionStandardBean = questionStandardDao2.findOne(listDocument.get(i)
                                                                                                    .getString("_id"));
                if (questionStandardBean != null) {
                    hitJson.put("v",
                                highlightQuestionTitles(questionStandardBean.getTitle(),
                                                        listWords));
                    hitJson.put("score", listDocument.get(i).getDouble("s"));
                    hits.add(hitJson);
                } else {
                    logger.info("此" + listDocument.get(i).getString("_id") + "编号查询时未在数据库中找到");
                }
            }
            rsJson.put("relate", hits);
        }
        return rsJson;
    }
}
