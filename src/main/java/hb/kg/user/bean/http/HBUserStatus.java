package hb.kg.user.bean.http;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户状况，用于后台展示使用
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserStatus {
    @Id
    private String id;
    private Date lastLoginDate; // 用户上次登陆的日期
    private String lastLoginIp; // 用户上次登陆IP地址
    private Integer totalLoginTime; // 总共登陆的次数
    @Transient
    private String lastLoginLocation; // 用户上次登陆的地址
}
