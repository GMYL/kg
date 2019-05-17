package hb.kg.search.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.algo.nlp.trie.Forest;
import hb.kg.common.util.algo.nlp.trie.GetWord;
import hb.kg.common.util.algo.nlp.trie.Library;
import hb.kg.common.util.algo.nlp.trie.NlpWord;
import hb.kg.common.util.encrypt.RegexUtils;
import hb.kg.common.util.set.HBCollectionUtil;
import hb.kg.search.bean.enums.HBDictionaryExcelType;
import hb.kg.search.bean.mongo.HBDictionaryStopWord;
import hb.kg.search.bean.mongo.HBDictionaryWord;
import hb.kg.search.bean.mongo.HBQuestionStandard;
import hb.kg.search.dao.mongo.DictionaryStopWordDao;
import hb.kg.search.dao.mongo.DictionaryWordDao;
import hb.kg.search.dao.mongo.QuestionStandardDao;

@Service
public class DictionaryWordService extends BaseCRUDService<HBDictionaryWord> {
    @Resource
    private DictionaryWordDao dictionaryWordDao;
    @Resource
    private DictionaryStopWordDao dictionaryStopWordDao;
    @Resource
    private QuestionStandardDao questionStandardDao;

    @Override
    public BaseDao<HBDictionaryWord> dao() {
        return dictionaryWordDao;
    }

    /**
     * 词典森林
     */
    private Forest forest;
    private ReentrantReadWriteLock forestLock = new ReentrantReadWriteLock();

    /**
     * 从词典中加载所有词，形成森林，注意可以重复加载 当后台对各个词做了修正之后，这里可以相应调整 INFO 11-03
     * 改为异步加载，解决系统启动时加载速度过慢的问题
     */
//    @PostConstruct
    public String init() {
        String resultStr = "载入词典开始执行";
        new Thread() {
            public void run() {
                if (forestLock.writeLock().tryLock()) {
                    try {
                        if (forest != null) {
                            forest.clear();
                        }
                        Collection<HBDictionaryWord> words = dictionaryWordDao.findAll(HBCollectionUtil.getMapSplit("excelType",
                                                                                                                    HBDictionaryExcelType.WENDA.getName()));
                        forest = Library.makeForest(words.stream()
                                                         .map(word -> word.toNlpWord())
                                                         .collect(Collectors.toList()));
                        logger.info("重新载入词典成功，共" + words.size() + "个词");
                    } catch (Exception e) {
                        logger.error("词典生成失败", e);
                    } finally {
                        forestLock.writeLock().unlock();
                    }
                }
            };

            public void destroy() {
                if (forestLock.isWriteLockedByCurrentThread()) {
                    forestLock.writeLock().unlock();
                }
            };
        }.start();
        return resultStr;
    }

    /**
     * 更新每个词在所有问题标题中出现的次数
     */
    public String refreshOccurNum() {
        try {
            Collection<HBDictionaryWord> words = dictionaryWordDao.findAll(HBCollectionUtil.getMapSplit("excelType",
                                                                                                        HBDictionaryExcelType.WENDA.getName()));
            HashMap<String, Integer> allWords = new HashMap<>();
            words.stream().forEach(word -> {
                allWords.put(word.getWord(), 0);
            });
            CloseableIterator<HBQuestionStandard> itr = mongoTemplate.stream(Query.query(Criteria.where("valid")
                                                                                                 .is(true)),
                                                                             HBQuestionStandard.class);
            while (itr.hasNext()) {
                try {
                    HBQuestionStandard questand = itr.next();
                    if (questand.getTitle() != null) {
                        getWordMap(questand.getTitle()).entrySet().stream().forEach(entry -> {
                            NlpWord word = entry.getValue();
                            Integer occurNum = allWords.get(word.getName());
                            if (occurNum != null) {
                                allWords.put(word.getName(), occurNum + 1);
                            }
                        });
                    }
                } catch (Exception e) {
                }
            }
            allWords.entrySet().stream().forEach(entry -> {
                Update update = Update.update("occurNum", entry.getValue());
                mongoTemplate.updateFirst(Query.query(Criteria.where("word").is(entry.getKey())),
                                          update,
                                          HBDictionaryWord.class);
            });
            return "更新每个词在所有问题标题中出现的次数成功";
        } catch (Exception e) {
            return "更新每个词在所有问题标题中出现的次数失败";
        }
    }

    /**
     * 分词并获取词的GetWord对象，线程安全
     */
    public GetWord getWord(String content) {
        forestLock.readLock().lock();
        try {
            return forest.getWord(content);
        } finally {
            forestLock.readLock().unlock();
        }
    }

    /**
     * 从语句中提取词和词频，线程安全。注意这里的结果内，词频和词权重是分开的，需要后续补充计算
     */
    public Map<String, NlpWord> getWordMap(String content) {
        GetWord udg = getWord(content);
        Map<String, NlpWord> wordMap = new HashMap<>();
        if (udg != null) {
            String temp = null;
            while ((temp = udg.getAllWords()) != null) {
                NlpWord word = wordMap.get(temp);
                if (word != null) {
                    word.setNum(word.getNum() + 1);
                } else {
                    wordMap.put(temp, udg.getParam().newWord().numPP()); // 注意这里拿到的是新的词，我们可以在这些词上进行各种操作而不需要担心会影响到分词词典
                }
            }
        }
        return wordMap;
    }

    /**
     * 从语句中提取词和词频，这里不再进行封装，直接提取数量，供简单的场合使用
     */
    public Map<String, Integer> getWordCountMap(String content) {
        GetWord udg = getWord(content);
        Map<String, Integer> wcm = new HashMap<>();
        if (udg != null) {
            String word = null;
            while ((word = udg.getAllWords()) != null) {
                String wordTrim = word.trim();
                Integer count = wcm.get(wordTrim);
                if (count != null) {
                    wcm.put(wordTrim, count + 1);
                } else {
                    wcm.put(wordTrim, 1);
                }
            }
        }
        return wcm;
    }

    /*
     * 从语句中提取词的集合，线程安全
     */
    public Set<String> getWordSet(String content) {
        GetWord udg = getWord(content.replace("、", ""));
        Set<String> wordSet = new HashSet<>();
        if (udg != null) {
            String temp = null;
            while ((temp = udg.getAllWords()) != null) {
                wordSet.add(temp);
            }
        }
        return wordSet;
    }

    /**
     * 从语句中提取词的列表，线程安全
     */
    public List<String> getWordList(String content) {
        GetWord udg = getWord(content);
        List<String> wordList = new ArrayList<>();
        if (udg != null) {
            String temp = null;
            while ((temp = udg.getAllWords()) != null) {
                wordList.add(temp);
            }
        }
        return wordList;
    }

    /*
     * 从语句中提取词的集合Map<word, off>，线程安全gmy
     */
    public Map<String, Integer> getTitleMap(String title) {
        GetWord udg = getWord(title);
        Map<String, Integer> wordMap = new HashMap<>();
        if (udg != null) {
            String temp = null;
            while ((temp = udg.getAllWords()) != null) {
                wordMap.put(temp, udg.getOffe());
            }
        }
        return wordMap;
    }

    public List<HBDictionaryWord> getWordRankingByOccur() {
        List<HBDictionaryWord> list = mongoTemplate.find(Query.query(Criteria.where("occurNum")
                                                                             .ne(0)),
                                                         HBDictionaryWord.class);
        list.sort(new Comparator<HBDictionaryWord>() {
            public int compare(HBDictionaryWord word1,
                               HBDictionaryWord word2) {
                return word1.getOccurNum() - word2.getOccurNum();
            }
        });
        return list;
    }

    /**
     * 给智能问答出现的词的子类词降权[参考http://wiki.taxmall.com.cn/pages/viewpage.action?pageId=3506453]
     */
    public void lowerWordWeightBySubclass(Map<String, NlpWord> wordMap,
                                          String content) {
        if (wordMap != null) {
            StringBuffer fullSent = new StringBuffer();
            // 首先生成公用字符串
            wordMap.entrySet().stream().forEach(entry -> {
                fullSent.append(entry.getKey()).append(" "); // 用空格分割一下，然后进行数量的判断
            });
            String fullSentStr = fullSent.toString();
            // 在公共字符串中查询某个词出现的次数
            wordMap.entrySet().forEach(entry -> {
                int count = 0;
                int i = 0;
                while (true) {
                    if (fullSentStr.indexOf(entry.getKey(), i) == -1) {
                        break;
                    } else {
                        count++;
                        i = fullSentStr.indexOf(entry.getKey(), i) + 1;
                    }
                }
                NlpWord word = entry.getValue();
                if (count > 1) {
                    // 如果一个搜索词出现了，那么这个搜索词分词后的内部词统一进行降权
                    word.setWeight(word.getWeight() / (4 * (count - 1)));
                    word.setNum(word.getNum() / 2); // 最大匹配原则和重复出现原则同时出现时，将重复出现的数量减半，尽可能降低子词的权重值
                }
                // else {
                // // 提升首词的权重--暂时关闭2018-11-13 gmy
                // if (content.startsWith(word.getName())) {
                // word.setWeight(word.getWeight() * 1.5f + 0.5f);
                // }
                // }
            });
        }
    }

    /**
     * 处理近义词和词义
     */
    public void supplementSynonymsWords(Map<String, NlpWord> wordMap,
                                        String content) {
        if (wordMap != null) {
            List<HBDictionaryWord> dbWords = mongoTemplate.find(Query.query(Criteria.where("word")
                                                                                    .in(new ArrayList<>(wordMap.keySet()))),
                                                                HBDictionaryWord.class);
            if (dbWords != null && dbWords.size() > 0) {
                dbWords.stream().forEach(word -> {
                    // 首先处理近义词
                    if (MapUtils.isNotEmpty(word.getSynonyms())) {
                        word.getSynonyms().entrySet().forEach(w -> {
                            if (!wordMap.containsKey(w.getKey())) {
                                NlpWord nlpw = new NlpWord();
                                nlpw.setName(w.getKey());
                                nlpw.setWeight(word.getWeight() * w.getValue()); // 近义词权重是主词*近义度
                                nlpw.setNum(0.5); // 近义词出现半次
                                wordMap.put(nlpw.getName(), nlpw);
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * 词义升降权
     */
    public void uplowerNatureWeight(Map<String, NlpWord> wordMap,
                                    String content) {
        if (wordMap != null) {
            List<HBDictionaryWord> dbWords = mongoTemplate.find(Query.query(Criteria.where("word")
                                                                                    .in(new ArrayList<>(wordMap.keySet()))),
                                                                HBDictionaryWord.class);
            if (dbWords != null && dbWords.size() > 0) {
                dbWords.stream().forEach(word -> {
                    // 按照词性升降权
                    if (StringUtils.isNotBlank(word.getNature())) {
                        natureManuWeight(wordMap.get(word.getWord()), word.getNature());
                    }
                });
            }
        }
    }

    /**
     * 处理词义，按照不同词义进行提权
     */
    public void natureManuWeight(NlpWord word,
                                 String nature) {
        if (word != null && StringUtils.isNotBlank(nature)) {
            switch (nature) {
            case "nt":
            case "ns":
            case "nsf":
            case "nz":
                word.setWeight(word.getWeight() * 1.1); // 机构团体
            case "nr":
            case "nr1":
            case "nr2":
            case "nrj":
            case "nrf":
                word.setWeight(word.getWeight() * 1.05); // 人名
            case "n":
            case "nl":
            case "ng":
                word.setWeight(word.getWeight() * 1.2); // 名词
                break;
            case "v":
            case "vd":
            case "vn":
            case "vf":
            case "vx":
            case "vi":
            case "vl":
            case "vg":
                word.setWeight(word.getWeight() * 0.9); // 动词
                break;
            case "a":
            case "ad":
            case "an":
            case "ag":
            case "al":
                word.setWeight(word.getWeight() * 0.1); // 形容词
                break;
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
                word.setWeight(word.getWeight() * 0.2); // 代词
                break;
            case "q":
            case "qv":
            case "qt":
                word.setWeight(word.getWeight() * 0.7); // 量词
                break;
            case "c":
            case "cc":
                word.setWeight(word.getWeight() * 0.3); // 连词
                break;
            case "f":
                word.setWeight(word.getWeight() * 0.3); // 方位词
                break;
            default:
                break;
            }
        }
    }

    /**
     * 处理词义，按照不同词义进行删除
     */
    public boolean natureDel(String word,
                             String nature) {
        if (StringUtils.isNotBlank(word) && StringUtils.isNotBlank(nature)) {
            switch (nature) {
            case "nt":
            case "ns":
            case "nsf":
            case "nz":
            case "nr":
            case "nr1":
            case "nr2":
            case "nrj":
            case "nrf":
            case "n":
            case "nl":
            case "ng":
            case "v":
            case "vd":
            case "vn":
            case "vf":
            case "vx":
            case "vi":
            case "vl":
            case "vg":
                return true;
            case "a":
            case "ad":
            case "an":
            case "ag":
            case "al":
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
            case "q":
            case "qv":
            case "qt":
            case "c":
            case "cc":
            case "f":
                return false;
            default:
                break;
            }
        }
        return false;
    }

    private AtomicBoolean onGenerate = new AtomicBoolean(false);

    public String UpdateDict(String path) {
        if (!onGenerate.get()) {
            onGenerate.set(true);
            File file = new File(path);
            if (file.exists()) {
                String[] filenames = file.list();// 取得当前目录下所有文件和文件夹
                for (String filename : filenames) {
                    switch (filename) {
                    case "dict.txt":
                        write(path, filename);
                        break;
                    case "qadict.txt":
                        write(path, filename);
                        break;
                    case "stopdict.txt":
                        write(path, filename);
                        break;
                    case "qastopdict.txt":
                        write(path, filename);
                        break;
                    default:
                        break;
                    }
                }
                onGenerate.set(false);
                return path + "词库更新完毕";
            } else {
                return path + "**路径错误***";
            }
        } else {
            return "词库正在更新，请不要重复点击";
        }
    }

    public void write(String path,
                      String filename) {
        try {
            BufferedOutputStream bufferedOutputStream = null;
            FileOutputStream fileO = new FileOutputStream(path + filename);
            bufferedOutputStream = new BufferedOutputStream(fileO);
            String newLine = System.getProperty("line.separator");// 回车多环境支持
            List<HBDictionaryWord> dictionaryWords = new ArrayList<>();
            List<HBDictionaryStopWord> dictionaryStopWords = new ArrayList<>();
            if (filename.equals("dict.txt")) {// 法规文章扩展词添加
                dictionaryWords = mongoTemplate.find(new Query(Criteria.where("excelType")
                                                                       .ne(HBDictionaryExcelType.WENDA.getName())),
                                                     HBDictionaryWord.class);
            }
            if (filename.equals("qadict.txt")) {// 问答扩展词添加
                dictionaryWords = mongoTemplate.find(new Query(Criteria.where("excelType")
                                                                       .is(HBDictionaryExcelType.WENDA.getName())),
                                                     HBDictionaryWord.class);
            }
            if (filename.equals("stopdict.txt")) {// 法规文章停用词添加
                dictionaryStopWords = mongoTemplate.find(new Query(Criteria.where("excelType")
                                                                           .ne(HBDictionaryExcelType.WENDA.getName())),
                                                         HBDictionaryStopWord.class);
            }
            if (filename.equals("qastopdict.txt")) {// 问答停用词添加
                dictionaryStopWords = mongoTemplate.find(new Query(Criteria.where("excelType")
                                                                           .is(HBDictionaryExcelType.WENDA.getName())),
                                                         HBDictionaryStopWord.class);
            }
            for (HBDictionaryWord dict : dictionaryWords) {// 写入扩展词
                bufferedOutputStream.write(dict.getWord().getBytes("utf-8"));
                bufferedOutputStream.write(newLine.getBytes()); // 写入回车
            }
            for (HBDictionaryStopWord stopdict : dictionaryStopWords) {// 写入停用词
                bufferedOutputStream.write(stopdict.getWord().getBytes("utf-8"));
                bufferedOutputStream.write(newLine.getBytes()); // 写入回车
            }
            logger.info(path + filename + "***更新成功***");
            bufferedOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从本地词典加载近义词
     */
    public String loadSynonymsDicByPath(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] lineWords = line.split("\t");
                // 删掉单个字和不全是汉字的情况（比如名称内有标点符号或数字）
                HashSet<String> wordSet = new HashSet<>();
                for (int i = 0; i < lineWords.length; i++) {
                    String word = lineWords[i].trim();
                    if (word.length() > 1 && RegexUtils.isChinese(word)) {
                        wordSet.add(word);
                    }
                }
                // 检查每个词我们当前词库有没有，没有的输出到控制台
                wordSet.stream().forEach(newWord -> {
                    HBDictionaryWord dbWord = mongoTemplate.findOne(new Query(Criteria.where("word")
                                                                                      .is(newWord)),
                                                                    HBDictionaryWord.class);
                    if (dbWord == null) {
                        // 如果这个词当前词库没有，添加进去
                        dbWord = new HBDictionaryWord();
                        dbWord.prepareHBBean();
                        dbWord.setExcelType(HBDictionaryExcelType.WENDA.getName());// 默认都是问答
                        dbWord.setWord(newWord);
                    }
                    // 如果这个词还没有同义词，把当前词加进去
                    if (dbWord.getSynonyms() == null) {
                        Map<String, Double> synonyms = wordSet.stream()
                                                              .filter(w -> w != newWord)
                                                              .collect(Collectors.toMap(x -> x,
                                                                                        y -> 0.8)); // 默认权重一开始都是0.8
                        dbWord.setSynonyms(synonyms);
                    } else {
                        Map<String, Double> synonyms = dbWord.getSynonyms();
                        wordSet.stream().filter(w -> w != newWord).forEach(w -> {
                            Double dbWeight = synonyms.get(w);
                            if (dbWeight == null) {// 如果这个词已经有了同义词，不更新
                                // 如果这个词还不是当前词的同义词
                                synonyms.put(w, 0.8);
                            }
                        });
                    }
                    dictionaryWordDao.updateOne(dbWord); // 更新数据库的用户词典
                });
            }
            br.close();
            return "从" + path + "读取词典成功";
        } catch (Exception e) {
            logger.error("从" + path + "读取词典失败", e);
            return "从" + path + "读取词典失败";
        }
    }

    /**
     * 从本地加载词性词典
     */
    public String loadNatureDicByPath(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] lineWords = line.split("\t");
                if (lineWords.length == 3) {
                    // 删掉单个字和不全是汉字的情况（比如名称内有标点符号或数字）
                    String word = lineWords[0].trim();
                    if (word.length() > 1 && RegexUtils.isChinese(word)) {
                        // 判断这个词数据库中有没有
                        HBDictionaryWord dbWord = mongoTemplate.findOne(new Query(Criteria.where("word")
                                                                                          .is(word)),
                                                                        HBDictionaryWord.class);
                        if (dbWord == null) {
                            dbWord = new HBDictionaryWord();
                            dbWord.prepareHBBean();
                            dbWord.setExcelType(HBDictionaryExcelType.WENDA.getName());// 默认都是问答
                            dbWord.setWord(word);
                        }
                        dbWord.setNature(lineWords[1].trim());
                        dictionaryWordDao.updateOne(dbWord); // 更新数据库的用户词典
                    }
                }
            }
            br.close();
            return "从" + path + "读取词典成功";
        } catch (Exception e) {
            logger.error("从" + path + "读取词典失败", e);
            return "从" + path + "读取词典失败";
        }
    }
}
