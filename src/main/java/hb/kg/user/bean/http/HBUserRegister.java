package hb.kg.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注册需要的类
 */
@ApiModel(description = "用户注册需要的类")
@Data
@Document(collection = "hb_users")
public class HBUserRegister implements Serializable {
    private static final long serialVersionUID = 7377925203120751317L;
    @ApiModelProperty(value = "用户ID")
    @Id
    private String id;
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "密码")
    private String password;
    @ApiModelProperty(value = "手机号")
    private String phoneNum;
    @ApiModelProperty(value = "验证码")
    @Transient
    private String smsCode;
}
