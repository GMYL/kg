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
public class HBArticleForListNews extends BaseMgBean<HBArticleForListNews> implements Serializable {
    private static final long serialVersionUID = 2825125656077687007L;
    @Id
    private String id; // 文章的id号
    private String title; // 文章标题
    private String summary; // 文章摘要
    private String content; // 文章内容
    private Integer state; // 文章状态 //0.草稿箱 1.待审核 2.正常发布 3.回收站
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private Date publishTime;// 发布时间
    private Integer visitNum; // 文章点击次数
    private Integer likeNum; // 文章点赞次数
    private String place;// 地方
    private String taxType;// 法规
    private String source;// 文章来源
    private Boolean isTop;// 是否置顶
    private Boolean isRecommend;// 是否推荐
    private List<String> label;// 标签
    private String date; // 文章创作时间
    @Indexed
    private List<String> categorys;
    @Indexed
    private List<String> taxTag; // 税种标签
    @Indexed
    private List<String> regionTag; // 地域标签
    @DBRef
    private HBUserBasic author;
}
