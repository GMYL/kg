package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.util.http.HtmlTagFilterUtils;
import hb.kg.common.util.id.IDUtil;
import hb.kg.upload.bean.ExcelUploadBean;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章批量上传用到
 * @author GMY
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticleUpload extends ExcelUploadBean<HBArticleUpload> implements Serializable {
    private static final long serialVersionUID = -2660475775105759632L;
    @Id
    private String id; // 文章的id号
    private String title; // 文章标题
    private String content; // 文章内容
    private String summary; // 文章摘要
    private Integer state; // 文章状态 //0.草稿箱 1.待审核 2.正常发布 3.回收站
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private String source;// 文章来源
    private String date;// 文章创作时间
    private String articleauthor;// 文章作者
    private String labelother;// 备注标签
    @Indexed
    private List<String> categorys;
    @DBRef
    private HBUserBasic author;// 发布文章作者

    public void prepareBaseBean() {
        String contentFilter = HtmlTagFilterUtils.TagFilter(content);
        createTime = createTime == null ? new Date() : createTime;
        id = id == null ? IDUtil.generateTimedIDStr() : id;
        state = state == null ? 1 : state;
        summary = StringUtils.isNotEmpty(summary) ? summary
                : StringUtils.isNotEmpty(content)
                        ? contentFilter.length() > 150 ? contentFilter.substring(0, 150)
                                : contentFilter
                        : "文章暂无摘要";
    }
}
