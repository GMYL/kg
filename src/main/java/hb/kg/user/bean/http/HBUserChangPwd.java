package hb.kg.user.bean.http;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于修改用户的登陆信息： 手机号 邮箱 用户名
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HBUserChangPwd implements Serializable {
    private static final long serialVersionUID = -7268794177985060187L;
    private String id; // 用户id
    private String oldPass; // 旧密码
    private String newPass; // 新密码
    private String rePass; // 重复新密码
}