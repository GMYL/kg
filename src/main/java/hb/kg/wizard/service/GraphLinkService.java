package hb.kg.wizard.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.wizard.bean.mongo.HBGraphLink;
import hb.kg.wizard.dao.GraphLinkDao;

@Service
public class GraphLinkService extends BaseCRUDService<HBGraphLink> {
    @Resource
    private GraphLinkDao graphLinkDao;

    @Override
    public BaseMongoDao<HBGraphLink> dao() {
        return graphLinkDao;
    }

    /**
     * 删掉和一个节点相关的所有边
     */
    public void deleteAllLinkByNode(String nodeid) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("start").is(nodeid),
                                                    Criteria.where("end").is(nodeid)));
        graphLinkDao.removeAll(query);
    }

    /**
     * 交换一个边的起点和终点
     */
    public HBGraphLink reverseLink(String id) {
        HBGraphLink srclink = dao().findOne(id);
        if (srclink != null) {
            dao().removeOne(id);
            String end = srclink.getEnd();
            srclink.setId(srclink.getEnd() + "-" + srclink.getStart());
            srclink.setEnd(srclink.getStart());
            srclink.setStart(end);
            return dao().insert(srclink);
        }
        return null;
    }

    /**
     * 给定几个节点，删掉这几个节点之间所有相关边
     */
    public String deleteAllRelateLinks(List<String> nodeids) {
        if (CollectionUtils.isNotEmpty(nodeids)) {
            DeleteResult result = mongoTemplate.remove(Query.query(Criteria.where("id")
                                                                           .in(generateAllRelateLinksByNodes(nodeids))),
                                                       HBGraphLink.class);
            return "一共删除了" + result.getDeletedCount() + "条边";
        }
        return "没有找到可用边";
    }

    /**
     * 按照给顶点，生成能够生成的所有边
     */
    public List<String> generateAllRelateLinksByNodes(List<String> nodeids) {
        List<String> links = new ArrayList<>(nodeids.size() * 2);
        if (CollectionUtils.isNotEmpty(nodeids)) {
            for (String node1 : nodeids) {
                for (String node2 : nodeids) {
                    if (node1 != node2) {
                        links.add(node1 + "-" + node2);
                    }
                }
            }
        }
        return links;
    }
}