package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户激活需要的类
 */
@ApiModel(description = "用户激活需要的类")
@Data
@Document(collection = "hb_users")
public class HBUserActiva implements Serializable {
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
    @ApiModelProperty(value = "所属用户组")
    private List<String> group; // 所属用户组
    @ApiModelProperty(value = "用户手机是否验证")
    private Boolean phoneState; // 用户手机是否验证
    @ApiModelProperty(value = "用户注册日")
    private Date regDate; // 用户注册日
    @ApiModelProperty(value = "用户是否有效")
    private Boolean valid; // 用户是否有效
    @ApiModelProperty(value = "用户状态")
    private Integer state; // 用户状态是否为发卡用户，#正常用户、未激活、已激活#
    @ApiModelProperty(value = "验证码")
    @Transient
    private String smsCode;
}
