package hb.kg.content.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.http.HtmlTagFilterUtils;
import hb.kg.common.util.id.IDUtil;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "文章信息")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticle extends BaseMgBean<HBArticle> implements Serializable {
    private static final long serialVersionUID = -9087214526010481847L;
    @ApiModelProperty(value = "文章的id号")
    @Id
    private String id; // 文章的id号
    @ApiModelProperty(value = "文章标题")
    private String title; // 文章标题
    @ApiModelProperty(value = "文章内容")
    private String content; // 文章内容
    @ApiModelProperty(value = "文章摘要")
    private String summary; // 文章摘要
    @ApiModelProperty(value = "文章状态 //0.草稿箱 1.待审核 2.正常发布 3.回收站")
    private Integer state; // 文章状态 //0.草稿箱 1.待审核 2.正常发布 3.回收站
    @ApiModelProperty(value = "文章上传日期")
    private Date createTime; // 文章上传日期
    @ApiModelProperty(value = "文章最后一次更新日期")
    private Date updateTime; // 文章最后一次更新日期
    @ApiModelProperty(value = "发布时间")
    private Date publishTime;// 发布时间
    @ApiModelProperty(value = "文章点击次数")
    @Deprecated
    private Integer visitNum; // 文章点击次数
    @ApiModelProperty(value = "文章点赞次数")
    private Integer likeNum; // 文章点赞次数
    // private String category2;// 2级分类
    // private String category1;// 1级分类
    @ApiModelProperty(value = "地方")
    private String place;// 地方
    @ApiModelProperty(value = "法规")
    private String taxType;// 法规
    @ApiModelProperty(value = "文章来源")
    private String source;// 文章来源
    @ApiModelProperty(value = "是否置顶")
    private Boolean isTop;// 是否置顶
    @ApiModelProperty(value = "是否推荐")
    private Boolean isRecommend;// 是否推荐
    @ApiModelProperty(value = "标签")
    private List<String> label;// 标签
    @ApiModelProperty(value = "标题图片")
    private String logo;// 标题图片
    @ApiModelProperty(value = "文章创作时间")
    private String date;// 文章创作时间
    @ApiModelProperty(value = "文章作者")
    private String articleauthor;// 文章作者
    @ApiModelProperty(value = "备注标签")
    private String labelother;// 备注标签
    @ApiModelProperty(value = "文章分类")
    @Indexed
    private List<String> categorys;
    @ApiModelProperty(value = "税种标签")
    @Indexed
    private List<String> taxTag; // 税种标签
    @ApiModelProperty(value = "地域标签")
    @Indexed
    private List<String> regionTag; // 地域标签
    @ApiModelProperty(value = "评论", hidden = true)
    private List<HBComment> commons; // 评论
    @ApiModelProperty(value = "发布文章作者")
    @DBRef
    private HBUserBasic author;// 发布文章作者

    public void prepareBaseBean() {
        String contentFilter = HtmlTagFilterUtils.TagFilter(content);
        createTime = createTime == null ? new Date() : createTime;
        id = id == null ? IDUtil.generateTimedIDStr() : id;
        summary = StringUtils.isNotEmpty(summary) ? summary
                : StringUtils.isNotEmpty(content)
                        ? contentFilter.length() > 150 ? contentFilter.substring(0, 150)
                                : contentFilter
                        : "文章暂无摘要";
        state = state != null ? state : 0;
        // 如果文章新产生的时候就发布，那么发布时间和创建时间相等
        publishTime = publishTime != null ? publishTime : state == 2 ? createTime : null;
    }
}
