package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticleBasicList extends BaseMgBean<HBArticleBasicList> implements Serializable {
    private static final long serialVersionUID = -2416285767985059900L;
    @Id
    private String id; // 文章的id号
    private String title; // 文章标题
    private String summary; // 文章摘要
    private Integer state; // 文章状态 //0.草稿箱 1.待审核 2.正常发布 3.回收站
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private Date publishTime;// 发布时间
    @Indexed
    private List<String> categorys;
    @DBRef
    private HBUserBasic author;// 发布文章作者
}
