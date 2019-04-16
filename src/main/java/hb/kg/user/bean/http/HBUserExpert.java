package hb.kg.user.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专家的一些基本信息，用这个在查询中展示一些基本信息
 */
@ApiModel(description = "专家的一些基本信息，用这个在查询中展示一些基本信息")
@Data
@Document(collection = "hb_users")
public class HBUserExpert implements Serializable {
    private static final long serialVersionUID = 853383114145563186L;
    @ApiModelProperty(value = "用户id")
    @Id
    private String id; // 用户id
    @ApiModelProperty(value = "用户昵称")
    private String nickName;// 用户昵称
    @ApiModelProperty(value = "用户头像链接")
    private String logo; // 用户头像链接
    @ApiModelProperty(value = "用户注册日期")
    private Date date; // 用户注册日期
    @ApiModelProperty(value = "用户是否有效")
    private Boolean valid; // 用户是否有效
    @ApiModelProperty(value = "用户介绍")
    private String comment;// 用户介绍
    // 专家部分
    @ApiModelProperty(value = "专家评价")
    private Integer star; // 专家评价

    public HBUserExpert() {}

    public HBUserExpert(String id) {
        setId(id);
    }
}
