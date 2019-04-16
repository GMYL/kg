package hb.kg.content.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticleTalkBasic extends BaseMgBean<HBArticleForList> implements Serializable {
    private static final long serialVersionUID = -2368352995104032427L;
    @Id
    private String id;
    private String title;
    private Integer visitNum; // 文章点击次数
    private String articleauthor;// 文章作者
}
