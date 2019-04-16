package hb.kg.msg.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.msg.bean.mongo.HBMessageText;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_site_mails")
public class HBSiteMailBasic extends BaseMgBean<HBSiteMailBasic> implements Serializable {
    private static final long serialVersionUID = 8421325724838915984L;
    @Id
    private String id; // 站内信自己的id号
    @Indexed
    @DBRef
    private HBUserBasic to; // 接受者
    @Indexed
    @DBRef
    private HBMessageText messagetext; // 通知信息
    private String type; // 消息类型：私信（priletter）、系统通知（global）、服务消息、活动消息等
    private Boolean fromValid; // 对收件人是否可见
}
