package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "hb_users")
public class HBUserAdmin implements Serializable {
    private static final long serialVersionUID = 1926681181232419176L;
    @Id
    private String id; // 用户id
    private String userName; // 用户登录名
    private String logo; // 用户头像
    private String nickName; // 用户昵称，禁止过度使用
    private String trueName; // 真实姓名，禁止过度使用
    private List<String> group; // 所属用户组
}
