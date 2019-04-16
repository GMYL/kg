package hb.kg.upload.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.IndexOptions;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.upload.bean.mongo.HBTempMongoBean;

@Repository("tempMongoDao")
public class TempMongoDao extends BaseMongoDao<HBTempMongoBean> {
    /**
     * 每次启动的时候，都检查一下collection，看是否正常创建，没有的话自动创建一下
     */
    @PostConstruct
    public void postInit() {
        if (!mongoTemplate.collectionExists(getCollectionName())) {
            mongoTemplate.createCollection(getCollectionName());
            IndexOptions io = new IndexOptions();
            io.expireAfter(mainServer.conf().getMongoTempCleanPeriod(), TimeUnit.SECONDS);
            mongoTemplate.getCollection(getCollectionName())
                         .createIndex(new Document("insertTime", 1), io);
        }
    }

    public List<HBTempMongoBean> findManyByIdAndRemove(List<String> idList) {
        if (idList == null || idList.size() == 0) {
            return new ArrayList<>(1);
        }
        Query query = new Query(Criteria.where("id").in(idList));
        return mongoTemplate.findAllAndRemove(query, HBTempMongoBean.class, getCollectionName());
    }

    public void removeExistings(String userid,
                                List<String> idList) {
        Query query = new Query(Criteria.where("user").is(userid));
        query.addCriteria(Criteria.where("srcBean._id").in(idList));
        mongoTemplate.remove(query, HBTempMongoBean.class, getCollectionName());
    }
}
