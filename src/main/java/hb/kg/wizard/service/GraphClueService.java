package hb.kg.wizard.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import hb.kg.common.bean.mongo.BaseIdBean;
import hb.kg.wizard.bean.http.HBGraphWordWithLink;
import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import hb.kg.wizard.bean.mongo.HBGraphLink;

/**
 * 推理模块，今后特殊情况的按边的查询都从这里进行
 */
// 注意这个类不能继承任何框架，尽量也不要用任何Dao，这样将来在优化的时候才能把它优化为语法句法分析的工具
@Service
@SuppressWarnings("unused")
public class GraphClueService {
    private String db_link = HBGraphLink.class.getAnnotation(Document.class).collection();
    private String db_node = HBGraphBaseNode.class.getAnnotation(Document.class).collection();
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 按照某个边类型对一个节点进行扩展
     * @param direct 0->both,1->only start,2->only end
     */
    public HBGraphWordWithLink getRelateNodeByConcept(String id,
                                                      String linktype,
                                                      int direct) {
        HashMap<String, HBGraphLink> linkMap = new HashMap<>();
        HashMap<String, HBGraphBaseNode> nodeMap = new HashMap<>();
        final HashSet<String> nameset = new HashSet<>();
        nameset.add(id);
        // 首先查询这个节点相关的所有边
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("type").in(linktype));
        switch (direct) {
        case 0:
        case 1:
            query1.addCriteria(Criteria.where("start").is(id));
        case 2:
            query1.addCriteria(Criteria.where("end").is(id));
        default:
            break;
        }
        List<HBGraphLink> links = mongoTemplate.find(query1, HBGraphLink.class);
        if (CollectionUtils.isNotEmpty(links)) {
            links.stream().collect(Collectors.toMap(HBGraphLink::getId, Function.identity()));
        }
        if (CollectionUtils.isNotEmpty(links)) {
            links.forEach(link -> {
                nameset.add(link.getStart());
                nameset.add(link.getEnd());
            });
        }
        // 再统一获取这些节点
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("id").in(nameset));
        Collection<HBGraphBaseNode> nodes = mongoTemplate.find(query2, HBGraphBaseNode.class);
        if (CollectionUtils.isNotEmpty(nodes)) {
            nodes.stream().collect(Collectors.toMap(HBGraphBaseNode::getId, Function.identity()));
        }
        nameset.clear();
        return new HBGraphWordWithLink(nodeMap.values(), linkMap.values());
    }

    /**
     * 获取所有有边的节点，进行这种聚合的时候报错了，因为边和节点太多了，先暂时关闭，等到解决这类问题的时候再做 Total size of documents
     * in hb_graph_nodes matching { $match: { $and: [ { start: { $eq: null } }, {} ]
     * } } exceeds maximum document size' on server 192.168.1.70:8091. The full
     * response is { "ok" : 0.0, "errmsg" : "Total size of documents in
     * hb_graph_nodes matching { $match: { $and: [ { start: { $eq: null } }, {} ] }
     * } exceeds maximum document size", "code" : 4568, "codeName" : "Location4568"
     * } 可以对知识图谱的相关数据采用非mongo的存储形式
     */
    @Deprecated
    public Collection<String> getAllWordsWithoutLink() {
        List<AggregationOperation> ops = new ArrayList<>();
        // 注意这里必须用id，否则报错：
        // No property _id found for type HBGraphBaseNode! Did you mean 'id'?
        ops.add(Aggregation.lookup(db_node, "id", "start", "ns"));
        ops.add(Aggregation.match(Criteria.where("ns.0").exists(true)));
        ops.add(Aggregation.lookup(db_node, "id", "end", "ne"));
        ops.add(Aggregation.match(Criteria.where("ne.0").exists(true)));
        ops.add(Aggregation.project("id"));
        Aggregation aggregation = Aggregation.newAggregation(ops);
        AggregationResults<BaseIdBean> agrs = mongoTemplate.aggregate(aggregation,
                                                                      HBGraphBaseNode.class,
                                                                      BaseIdBean.class);
        List<String> list = new LinkedList<>();
        agrs.forEach(b -> {
            list.add(b.getId());
        });
        return list;
    }
}
