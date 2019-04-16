package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 在一些需要列表展示的场合，可以用这个在查询中展示一些基本信息
 */
@Data
@Document(collection = "hb_users")
public class HBUserSimple implements Serializable {
    private static final long serialVersionUID = 853383114145563186L;
    @Id
    private String id; // 用户id
    private String nickName;// 用户昵称
    private String logo; // 用户头像链接
    private Date date; // 用户注册日期
    private Boolean valid; // 用户是否有效
}
