package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提供复杂查询，供后台查询文章使用
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticleMultiSearch extends BaseMgBean<HBArticleMultiSearch> implements Serializable {
    private static final long serialVersionUID = 6962463668937520467L;
    @Id
    private String id;
    private String title;
    private List<Date> createTime;
    private List<String> categorys;
    private Integer state;
    @DBRef
    private HBUserBasic author;// 发布文章作者
    private String date; // 文章创作时间
    private String articleauthor;// 文章作者
}
