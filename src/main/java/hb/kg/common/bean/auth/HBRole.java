package hb.kg.common.bean.auth;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我们这里使用两重验证，如果是配了jwtRole的，使用jwtRole进行验证，否则我们自己来设置验证方式
 */
@ApiModel(description = "角色")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_roles")
public class HBRole extends BaseMgBean<HBRole> implements Serializable {
    private static final long serialVersionUID = 1315568341117606585L;
    @ApiModelProperty(value = "角色ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "展示名称")
    private String name; // 展示名称
    @ApiModelProperty(value = "jwt")
    private String jwt;
    @ApiModelProperty(value = "允许通过的url")
    @Indexed
    private List<String> modules; // 允许通过的url
    @ApiModelProperty(value = "modules的介绍")
    private List<String> modulesname; // modules的介绍
}
