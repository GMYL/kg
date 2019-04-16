package hb.kg.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于修改用户的登陆信息： 手机号 邮箱 用户名
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserAccount extends BaseMgBean<HBUserAccount> implements Serializable {
    private static final long serialVersionUID = -7268794177985060187L;
    @Id
    private String id; // 用户id
    private String userName; // 用户名
    private String phoneNum; // 用户手机号
    private String email; // 用户邮箱
    private String password; // 用户输入密码
    @Transient
    private String smsCode; // 验证码
}