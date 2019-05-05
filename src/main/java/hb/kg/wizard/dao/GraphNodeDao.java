package hb.kg.wizard.dao;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.wizard.bean.mongo.HBGraphBaseNode;

/**
 * 为了后面兼容更多种类的节点，节点种类不同的影响封装到这个层次内部
 */
@Repository
public class GraphNodeDao extends BaseMongoDao<HBGraphBaseNode> {
   

    public Collection<HBGraphBaseNode> findAllByType(Collection<String> ids,
                                                     Object... nodetype) {
        if (CollectionUtils.isNotEmpty(ids)) {
            Query query = new Query();
            if (nodetype != null && nodetype.length > 0) {
                query.addCriteria(Criteria.where("type").in(nodetype));
            }
            query.addCriteria(Criteria.where("word").in(ids));
            return mongoTemplate.find(query, HBGraphBaseNode.class);
        } else {
            return null;
        }
    }
}
