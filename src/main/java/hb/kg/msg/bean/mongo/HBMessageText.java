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

@ApiModel(description = "通知信息")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_site_messagetext")
public class HBMessageText extends BaseMgBean<HBMessageText> implements Serializable {
    private static final long serialVersionUID = 5450323134269720167L;
    @ApiModelProperty(value = "信息ID")
    @Id
    private String id; // 通知自己的id
    @Indexed
    @ApiModelProperty(value = "发送者", hidden = true)
    @DBRef
    private HBUserBasic from; // 发送者
    @ApiModelProperty(value = "标题")
    private String title; // 标题
    @ApiModelProperty(value = "内容")
    private String content; // 内容
    @ApiModelProperty(value = "发送时间")
    private Date createTime; // 发送时间
    @ApiModelProperty(value = "消息类型")
    private String type; // 消息类型：私信（priletter）、系统通知（global）、服务消息、活动消息等
    @ApiModelProperty(value = "对发件人是否可见（删除用到）")
    private Boolean fromValid; // 对发件人是否可见（删除用到）

    @Override
    public void prepareHBBean() {
        id = id == null ? IDUtil.generateTimedIDStr() : id;
        createTime = createTime != null ? createTime : new Date();
        title = title == null ? "无标题" : title;
        type = type == null ? "global" : type;
        fromValid = fromValid == null ? true : fromValid;
    }
}
