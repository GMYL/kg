package hb.kg.wizard.bean.http;

import java.io.Serializable;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单词
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_graph_nodes")
public class GraphWordHTTP extends HBGraphBaseNode implements Serializable {
    private static final long serialVersionUID = -8324600940153969675L;
    @Indexed
    private String word; // 全词，只用在当对象是单词的时候
    private String nature; // 词性
}
