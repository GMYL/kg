package hb.kg.content.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseTreeMgBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_article_categorys")
public class HBArticleCategory extends BaseTreeMgBean<HBArticleCategory> implements Serializable {
    private static final long serialVersionUID = -6996120749924735054L;
    @Id
    private String id;
    protected Integer sequence; // 排序值
}
