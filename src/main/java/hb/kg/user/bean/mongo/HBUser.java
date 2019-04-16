package hb.kg.user.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import hb.kg.content.bean.http.HBArticleBasic;
import hb.kg.user.bean.enums.HBUserStateEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库中用户的基础类，承载用户的基础数据
 */
@ApiModel(description = "用户基础信息")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUser extends BaseMgBean<HBUser> implements Serializable {
    private static final long serialVersionUID = -866403171737037890L;
    // 账号信息
    @ApiModelProperty(value = "用户id")
    @Id
    private String id; // 用户id
    @ApiModelProperty(value = "用户登录名，#可登陆#")
    @Indexed(unique = true, dropDups = true)
    private String userName; // 用户登录名，#可登陆#
    @ApiModelProperty(value = "用户手机号，#可登录#")
    @Indexed(unique = true, dropDups = true)
    private String phoneNum; // 用户手机号，#可登录#
    @ApiModelProperty(value = "用户邮箱，#可登录#")
    @Indexed(unique = true, dropDups = true)
    private String email; // 用户邮箱，#可登录#
    @ApiModelProperty(value = "用户状态")
    private Integer state; // 用户状态是否为发卡用户，#正常用户、未激活、已激活#
    @ApiModelProperty(value = "用户手机是否验证")
    private Boolean phoneState; // 用户手机是否验证
    @ApiModelProperty(value = "用户邮箱是否验证")
    private Boolean emailState; // 用户邮箱是否验证
    @ApiModelProperty(value = "用户注册日")
    private Date regDate; // 用户注册日
    @ApiModelProperty(value = "用户上一次修改密码的日期，jwt要用")
    private Date lastPasswordResetDate; // 用户上一次修改密码的日期，jwt要用
    @ApiModelProperty(value = "用户密码")
    private String password; // 用户密码
    @ApiModelProperty(value = "用户是否有效")
    private Boolean valid; // 用户是否有效
    @ApiModelProperty(value = "用户qq号")
    private String qq; // 用户qq号
    @ApiModelProperty(value = "用户微信账号")
    private String wechat; // 用户微信账号
    @ApiModelProperty(value = "用户上次登陆的日期")
    private Date lastLoginDate; // 用户上次登陆的日期
    @ApiModelProperty(value = "用户上次登陆IP地址")
    private String lastLoginIp; // 用户上次登陆IP地址
    @ApiModelProperty(value = "总共登陆的次数")
    private Integer totalLoginTime; // 总共登陆的次数
    // 个性化信息
    @ApiModelProperty(value = "用户年龄")
    private Integer age; // 用户年龄
    @ApiModelProperty(value = "用户生日")
    private Date birthday; // 用户生日
    @ApiModelProperty(value = "性别")
    private String gender; // 性别
    @ApiModelProperty(value = "用户头像")
    private String logo; // 用户头像
    @ApiModelProperty(value = "用户个人签名，短（追在头像后面 30个字）")
    private String comment; // 用户个人签名，短（追在头像后面 30个字）
    @ApiModelProperty(value = "个人简介，长")
    private String profile; // 个人简介，长
    @ApiModelProperty(value = "用户昵称")
    private String nickName; // 用户昵称
    @ApiModelProperty(value = "真实姓名，禁止过度使用")
    private String trueName; // 真实姓名，禁止过度使用
    @ApiModelProperty(value = "用户地址")
    private String address; // 用户地址
    // 用户网站角色
    @ApiModelProperty(value = "用户个人所属公司（名称可以任意）")
    private String company; // 用户个人所属公司（名称可以任意）
    @ApiModelProperty(value = "用户职位")
    private String position; // 用户职位
    @ApiModelProperty(value = "用户等级")
    private Integer level; // 用户等级
    @ApiModelProperty(value = "用户积分")
    private Long credits; // 用户积分
    @ApiModelProperty(value = "大V认证")
    private String identity; // 大V认证
    // VIP的有两个位置判断，一个是这里，另一个是用户是否属于VIP用户组
    @ApiModelProperty(value = "会员级别")
    private Integer vip; // 会员，0-非会员，1-普通会员，...
    @ApiModelProperty(value = "会员状态", hidden = true)
    private HBUserVipState vipState; // 会员状态
    // 微信
    @ApiModelProperty(value = "微信id")
    private String openId; // 微信id
    // 后台管理功能
    @ApiModelProperty(value = "角色")
    @Indexed
    private List<String> roles; // 角色
    @ApiModelProperty(value = "所属用户组")
    @Indexed
    private List<String> group; // 所属用户组
    // 专家部分
    @ApiModelProperty(value = "专家评价")
    private Integer star; // 专家评价
    // 个人中心
    @ApiModelProperty(value = "我收藏的文章", hidden = true)
    @DBRef
    private List<HBArticleBasic> focusArticles; // 我收藏的文章

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        this.id = StringUtils.isEmpty(this.id) ? IDUtil.generateTimedIDStr() : this.id;
        this.userName = StringUtils.isEmpty(this.userName) ? "hb_" + this.id : this.userName;
        this.phoneNum = StringUtils.isEmpty(this.phoneNum) ? "unset_" + IDUtil.generateRandomKey(9)
                : this.phoneNum;
        this.email = StringUtils.isEmpty(this.email) ? "unset_" + IDUtil.generateRandomKey(9)
                : this.email;
        this.regDate = new Date();
        this.valid = this.valid != null ? this.valid : true;
        this.vip = this.vip != null ? this.vip : 0;
        this.state = this.state != null ? this.state : HBUserStateEnum.USER_ROLE.getIndex();// 正常用户
    }
}
