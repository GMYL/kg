package hb.kg.wizard.dao;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.wizard.bean.mongo.HBGraphLink;

/**
 * 这里默认查询时不需要边条件的
 */
@Repository("graphLinkDao")
public class GraphLinkDao extends BaseMongoDao<HBGraphLink> {
    /**
     * 查询关于一个节点id的所有连接，无论这个节点有没有方向性
     */
    public Collection<HBGraphLink> findAllLinksByNode(String nodeid,
                                                      Object... linktype) {
        if (StringUtils.isNotBlank(nodeid)) {
            Query query = new Query();
            if (linktype != null && linktype.length > 0) {
                query.addCriteria(Criteria.where("type").in(linktype));
            }
            query.addCriteria(new Criteria().orOperator(Criteria.where("start").is(nodeid),
                                                        Criteria.where("end").is(nodeid)));
            return mongoTemplate.find(query, HBGraphLink.class);
        } else {
            return null;
        }
    }

    /**
     * 查询关于N个节点id的所有连接，无论这个节点有没有方向性
     */
    public Collection<HBGraphLink> findAllLinksByNodes(Collection<String> nodeids,
                                                       Object... linktype) {
        if (CollectionUtils.isNotEmpty(nodeids)) {
            Query query = new Query();
            if (linktype != null && linktype.length > 0) {
                query.addCriteria(Criteria.where("type").in(linktype));
            }
            query.addCriteria(new Criteria().orOperator(Criteria.where("start").in(nodeids),
                                                        Criteria.where("end").in(nodeids)));
            return mongoTemplate.find(query, HBGraphLink.class);
        } else {
            return null;
        }
    }

    /**
     * 查询关于一个节点id的所有对外连接，这个节点必须是有向的
     */
    public Collection<HBGraphLink> findAllOutLinksByNode(String nodeid,
                                                         Object... linktype) {
        if (StringUtils.isNotBlank(nodeid)) {
            Query query = new Query();
            if (linktype != null && linktype.length > 0) {
                query.addCriteria(Criteria.where("type").in(linktype));
            }
            query.addCriteria(Criteria.where("start").is(nodeid));
            query.addCriteria(Criteria.where("direct").is(0));
            return mongoTemplate.find(query, HBGraphLink.class);
        } else {
            return null;
        }
    }

    /**
     * 查询关于一个节点的所有朝内连接，这个节点必须是有向的
     */
    public Collection<HBGraphLink> findAllInLinksByNode(String nodeid,
                                                        Object... linktype) {
        if (StringUtils.isNotBlank(nodeid)) {
            Query query = new Query();
            if (linktype != null && linktype.length > 0) {
                query.addCriteria(Criteria.where("type").in(linktype));
            }
            query.addCriteria(Criteria.where("end").is(nodeid));
            query.addCriteria(Criteria.where("direct").is(0));
            return mongoTemplate.find(query, HBGraphLink.class);
        } else {
            return null;
        }
    }

    /**
     * 查询关于一个节点的所有无向连接，这个节点必须是无向的
     */
    public Collection<HBGraphLink> findAllUnDirectLinksByNode(String nodeid,
                                                              Object... linktype) {
        if (StringUtils.isNotBlank(nodeid)) {
            Query query = new Query();
            if (linktype != null && linktype.length > 0) {
                query.addCriteria(Criteria.where("type").in(linktype));
            }
            query.addCriteria(Criteria.where("direct").is(1));
            return mongoTemplate.find(query, HBGraphLink.class);
        } else {
            return null;
        }
    }
}