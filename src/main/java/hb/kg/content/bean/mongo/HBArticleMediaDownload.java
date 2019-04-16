package hb.kg.content.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章内容里的图片等媒体文件
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_article_media")
public class HBArticleMediaDownload extends BaseMgBean<HBArticleMediaDownload>
        implements Serializable {
    private static final long serialVersionUID = 6951984764303793372L;
    @Id
    private String id; // id号（按文件名加密）
    private String title; // 文件名
    private String filename; // 文件原始名称
    private String filetype; // 文件类型后缀
    private String url; // 文件访问url
    private String size; // 文件大小
    private Date createTime; // 文件上传时间
    @DBRef
    private HBUserBasic author; // 上传用户
    @Transient
    private List<Date> listCreateTime;// 文件上传时间

    // 补充一些没有添加的数据
    public void prepareBaseBean() {
        id = StringUtils.isEmpty(id) ? IDUtil.generateTimedIDStr() : id;
        createTime = createTime == null ? new Date() : createTime;
    }
}
