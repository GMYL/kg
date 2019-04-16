package hb.kg.msg.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "站内信")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_site_mails")
public class HBSiteMail extends BaseMgBean<HBSiteMail> implements Serializable {
    private static final long serialVersionUID = 8421325724838915984L;
    @ApiModelProperty(value = "站内信ID")
    @Id
    private String id; // 站内信自己的id号
    @ApiModelProperty(value = "发送者")
    @Indexed
    @DBRef
    private HBUserBasic from; // 发送者
    @ApiModelProperty(value = "接受者")
    @Indexed
    @DBRef
    private HBUserBasic to; // 接受者
    @ApiModelProperty(value = "标题")
    private String title; // 标题
    @ApiModelProperty(value = "内容")
    private String content; // 内容
    @ApiModelProperty(value = "内容")
    private Date createTime; //
    @ApiModelProperty(value = "接受者是否已读")
    private Boolean isRead; // 接受者是否已读
    @ApiModelProperty(value = "通知信息")
    @DBRef
    private HBMessageText messagetext; // 通知信息
    @ApiModelProperty(value = "消息类型")
    private String type; // 消息类型：私信（priletter）、系统通知（global）、服务消息、活动消息等
    @ApiModelProperty(value = "对收件人是否可见")
    private Boolean toValid; // 对收件人是否可见
    @ApiModelProperty(value = "对发件人是否可见")
    private Boolean fromValid; // 对发件人是否可见

    @Override
    public void prepareHBBean() {
        id = id == null ? IDUtil.generateRandomKey(9) : id;
        createTime = createTime != null ? createTime : new Date();
        isRead = isRead == null ? false : isRead;
        toValid = toValid == null ? true : toValid;
        fromValid = fromValid == null ? true : fromValid;
        title = title == null ? "无标题" : title;
        type = type == null ? "priletter" : type;
    }
}
