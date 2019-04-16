package hb.kg.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 在一些基本的场合，提供用户最基础的信息返回
 */
@ApiModel(description = "用户最基础的信息")
@Data
@Document(collection = "hb_users")
public class HBUserBasic implements Serializable {
    private static final long serialVersionUID = 1926681181232419176L;
    @ApiModelProperty(value = "用户ID")
    @Id
    private String id; // 用户id
    @ApiModelProperty(value = "用户登录名")
    private String userName; // 用户登录名
    @ApiModelProperty(value = "用户昵称")
    private String nickName; // 用户昵称
    @ApiModelProperty(value = "用户真实姓名")
    private String trueName; // 用户真实姓名

    public HBUserBasic() {}

    public HBUserBasic(String id) {
        setId(id);
    }
}
