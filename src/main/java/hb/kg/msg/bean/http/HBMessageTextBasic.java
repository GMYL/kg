package hb.kg.msg.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_site_messagetext")
public class HBMessageTextBasic extends BaseMgBean<HBMessageTextBasic> implements Serializable {
    private static final long serialVersionUID = 5450323134269720167L;
    @Id
    private String id; // 通知自己的id
    private String title; // 标题
    private Date createTime; // 发送时间
    private String type; // 消息类型：私信（priletter）、系统通知（global）、服务消息、活动消息等
    private Boolean fromValid; // 对收件人是否可见
    @Transient
    private Boolean isRead; // 显示的时候，是否已读
}
