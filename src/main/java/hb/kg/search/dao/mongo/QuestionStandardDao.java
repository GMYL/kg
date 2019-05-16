package hb.kg.search.dao.mongo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.search.bean.http.HBQuestionStandardBasic;
import hb.kg.search.bean.mongo.HBQuestionStandard;

@Repository("questionStandardDao")
public class QuestionStandardDao extends BaseMongoDao<HBQuestionStandard> {
    public List<HBQuestionStandardBasic> getHotQuestions(int top) {
        Query query = new Query();
        query.with(Sort.by(new Order(Direction.DESC, "visitNum")));
        query.limit(top);
        return mongoTemplate.find(query, HBQuestionStandardBasic.class, getCollectionName());
    }

    public List<HBQuestionStandardBasic> getNewestQuestions(int top) {
        Query query = new Query();
        query.with(Sort.by(new Order(Direction.DESC, "createTime")));
        query.limit(top);
        return mongoTemplate.find(query, HBQuestionStandardBasic.class, getCollectionName());
    }
}
