package hb.kg.user.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.user.bean.mongo.HBUser;

@Repository("userDao")
public class UserDao extends BaseMongoDao<HBUser> {
    public HBUser findUserByNameAndPwd(String username,
                                       String password) {
        Query query = new Query();
        Criteria c1 = Criteria.where("name").is(username);
        Criteria c2 = Criteria.where("password").is(password);
        query.addCriteria(new Criteria().andOperator(c1, c2));
        return mongoTemplate.findOne(query, getClassT(), getCollectionName());
    }

    public HBUser findUserById(String id) {
        return findOne(id);
    }

    public HBUser findUserByName(String name) {
        return mongoTemplate.findOne(new Query(Criteria.where("userName").is(name)),
                                     getClassT(),
                                     getCollectionName());
    }

    public HBUser findUserByPhoneNum(String phoneNum) {
        return mongoTemplate.findOne(new Query(Criteria.where("phoneNum").is(phoneNum)),
                                     getClassT(),
                                     getCollectionName());
    }

    public HBUser findUserByPsdAndNameOrPhoneOrEmail(String name,
                                                     String password) {
        Query query = new Query();
        Criteria c1 = Criteria.where("userName").is(name);
        Criteria c2 = Criteria.where("phoneNum").is(name);
        // Criteria c3 = Criteria.where("email").is(name);
        // query.addCriteria(Criteria.where("password").is(password).orOperator(c1, c2,
        // c3));
        query.addCriteria(Criteria.where("password").is(password).orOperator(c1, c2));
        return mongoTemplate.findOne(query, getClassT(), getCollectionName());
    }

    public HBUser findUserByNameOrPhoneOrEmail(String name) {
        Query query = new Query();
        Criteria c1 = Criteria.where("userName").is(name);
        Criteria c2 = Criteria.where("phoneNum").is(name);
        // Criteria c3 = Criteria.where("email").is(name);
        // query.addCriteria(new Criteria().orOperator(c1, c2, c3));
        query.addCriteria(new Criteria().orOperator(c1, c2));
        return mongoTemplate.findOne(query, getClassT(), getCollectionName());
    }
}
