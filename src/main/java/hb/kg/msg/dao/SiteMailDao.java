package hb.kg.msg.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.msg.bean.mongo.HBSiteMail;

@Repository("siteMailDao")
public class SiteMailDao extends BaseMongoDao<HBSiteMail> {
    public boolean removeMyMail(String userid,
                                String mailid) {
        Query query = new Query();
        Criteria c1 = Criteria.where("toId").is(userid);
        Criteria c2 = Criteria.where("fromId").is(userid);
        query.addCriteria(Criteria.where("id").is(mailid).orOperator(c1, c2));
        mongoTemplate.remove(query, getClassT(), getCollectionName());
        return true;
    }
}
