package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.mongo.HBUserVipState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户登陆需要的类
 */
@ApiModel(description = "用户登陆需要的类")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserLogin extends BaseMgBean<HBUserLogin> implements Serializable {
    private static final long serialVersionUID = 4034391602063997660L;
    @ApiModelProperty(value = "用户id")
    @Id
    private String id; // 用户id
    @ApiModelProperty(value = "用户名")
    private String userName; // 用户名
    @ApiModelProperty(value = "用户密码")
    private String password; // 用户密码
    @ApiModelProperty(value = "用户当前的jwt token")
    private String jwtToken; // 用户当前的jwt token，非存数据库
    @ApiModelProperty(value = "手机验证码")
    @Transient
    private String smsCode; // 手机验证码，非存数据库
    @ApiModelProperty(value = "用户手机号")
    private String phoneNum; // 用户手机号
    @ApiModelProperty(value = "用户邮箱")
    private String email; // 用户邮箱
    @ApiModelProperty(value = "用户年龄")
    private Integer age; // 用户年龄
    @ApiModelProperty(value = "性别")
    private String gender; // 性别
    @ApiModelProperty(value = "用户注册日期")
    private Date regDate; // 用户注册日期
    @ApiModelProperty(value = "用户是否有效")
    private Boolean valid; // 用户是否有效
    @ApiModelProperty(value = "角色")
    private List<String> roles; // 角色
    @ApiModelProperty(value = "所属用户组")
    private List<String> group; // 所属用户组
    @ApiModelProperty(value = "用户所属公司")
    private String company; // 用户所属公司
    @ApiModelProperty(value = "用户等级")
    private Integer level; // 用户等级
    @ApiModelProperty(value = "用户昵称")
    private String nickName;// 用户昵称
    @ApiModelProperty(value = "用户个人签名，短（追在头像后面 30个字）")
    private String comment; // 用户个人签名，短（追在头像后面 30个字）
    @ApiModelProperty(value = "用户头像链接")
    private String logo; // 用户头像链接
    @ApiModelProperty(value = "会员级别")
    private Integer vip; // 会员，0-非会员，1-普通会员，...
    @ApiModelProperty(value = "会员状态", hidden = true)
    private HBUserVipState vipState; // 会员状态
    @ApiModelProperty(value = "微信openId")
    private String openId; // 微信openId
    @ApiModelProperty(value = "用户所拥有的module，加载的时候填充")
    @Transient
    private List<String> modules; // 用户所拥有的module，加载的时候填充
}
