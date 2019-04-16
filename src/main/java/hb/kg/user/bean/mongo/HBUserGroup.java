package hb.kg.user.bean.mongo;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户组 包括用户组的权限、组长、副组长等，用户组的具体组员则由用户表的parent实现方式实现
 */
@ApiModel(description = "用户组")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_user_groups")
public class HBUserGroup extends BaseMgBean<HBUserGroup> implements Serializable {
    private static final long serialVersionUID = -1812018499878113740L;
    @ApiModelProperty(value = "组id")
    @Id
    private String id; // 组id
    @ApiModelProperty(value = "展示名称")
    private String name; // 展示名称
    @ApiModelProperty(value = "组长", hidden = true)
    @DBRef
    private HBUserBasic leader; // 组长
    @ApiModelProperty(value = "副组长", hidden = true)
    @DBRef
    private List<HBUserBasic> viceLeaders; // 副组长
    @ApiModelProperty(value = "该用户组具有的角色", hidden = true)
    @Indexed
    private List<String> roles; // 该用户组具有的角色
    @ApiModelProperty(value = "该用户组具有的角色介绍", hidden = true)
    private List<String> rolesname; // 该用户组具有的角色介绍
    @ApiModelProperty(value = "该用户组的作用", hidden = true)
    private String feature; // 该用户组的作用
}
