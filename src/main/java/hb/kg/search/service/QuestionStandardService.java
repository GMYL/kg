package hb.kg.search.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.set.HBCollectionUtil;
import hb.kg.search.bean.http.HBQuestionStandardBasic;
import hb.kg.search.bean.mongo.HBQuestionStandard;
import hb.kg.search.dao.mongo.QuestionStandardDao;

@Service
public class QuestionStandardService extends BaseCRUDService<HBQuestionStandard> {
    @Resource
    private QuestionStandardDao questionStandardDao;

    @Override
    public BaseDao<HBQuestionStandard> dao() {
        return questionStandardDao;
    }

    @Autowired
    private DictionaryWordService dictionaryWordService;

    /**
     * 补充插入一个bean需要补充的信息
     */
    public HBQuestionStandard initQuestionStandard(HBQuestionStandard question) {
        return null;
    }

    public List<HBQuestionStandardBasic> getHotQuestions(int top) {
        return questionStandardDao.getHotQuestions(top);
    }

    public List<HBQuestionStandardBasic> getNewestQuestions(int top) {
        return questionStandardDao.getNewestQuestions(top);
    }

    /**
     * 刷新当前库内的问答，利用当前标题生成暗标题
     */
    public String generateDarkTitle() {
        CloseableIterator<HBQuestionStandard> itr = mongoTemplate.stream(new Query(Criteria.where("title")
                                                                                           .exists(true)),
                                                                         HBQuestionStandard.class);
        int success = 0;
        int failed = 0;
        while (itr.hasNext()) {
            try {
                HBQuestionStandard question = itr.next();
                String darkTitle = HBCollectionUtil.listToString(dictionaryWordService.getWordSet(question.getTitle()),
                                                                 " ");
                mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(question.getId())),
                                          new Update().set("darkTitle", darkTitle),
                                          HBQuestionStandard.class);
                success++;
            } catch (Exception e) {
                logger.error("生成暗标题失败", e);
                failed++;
            }
        }
        itr.close();
        return "生成成功" + success + "个，生成失败" + failed + "个";
    }

    /**
     * 解决其它模块数据调用，这里可以加入截断，对用户的调用数据进行截断并本类的特有功能
     */
    public Object solveData(JSONObject data) {
        return super.solveData(data);
    }
}
