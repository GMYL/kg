package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.mongo.HBUserVipState;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户修改自己的信息用到的类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserSelfEdit extends BaseMgBean<HBUserSelfEdit> implements Serializable {
    private static final long serialVersionUID = -9013527242255706545L;
    @Id
    private String id; // 用户id
    private String userName; // 用户名
    private String phoneNum; // 用户手机号
    private String email; // 用户邮箱
    private String gender; // 性别
    private String qq; // 用户qq号
    private String wechat; // 用户微信账号
    private Integer age; // 用户年龄
    private Date birthday; // 用户生日
    private String logo; // 用户头像
    private String comment; // 用户个人签名，短（追在头像后面 30个字）
    private String profile; // 个人简介，长
    private String nickName; // 用户昵称，禁止过度使用
    private String trueName; // 真实姓名，禁止过度使用
    private String company; // 用户所属公司
    private String position; // 用户职位
    private Integer level; // 用户等级
    private Integer vip; // 会员，0-非会员，1-普通会员，...
    private HBUserVipState vipState; // 会员状态
   
}
