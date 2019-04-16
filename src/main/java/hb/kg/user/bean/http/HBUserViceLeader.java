package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 用户组管理副组长显示做一下处理
 */
@Data
@Document(collection = "hb_users")
public class HBUserViceLeader implements Serializable {
    private static final long serialVersionUID = 7507000790793264500L;
    @Id
    private String id; // 用户id
    private String userName; // 用户登录名
    private String nickName; // 用户昵称
    private String trueName; // 用户真实姓名
    @Indexed
    private List<String> group; // 所属用户组

    public static HBUserBasic toHBUserBasic(HBUserViceLeader ViceLeader) {
        HBUserBasic hbUserBasic = new HBUserBasic();
        if (ViceLeader != null) {
            hbUserBasic.setId(ViceLeader.id);
            hbUserBasic.setNickName(ViceLeader.nickName);
            hbUserBasic.setTrueName(ViceLeader.trueName);
            hbUserBasic.setUserName(ViceLeader.userName);
        }
        return hbUserBasic;
    }
}
