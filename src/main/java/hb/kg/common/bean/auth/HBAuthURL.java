package hb.kg.common.bean.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 一个判断权限有一个url和method
 */
@ApiModel(description = "URL")
@Data
public class HBAuthURL {
    @ApiModelProperty(value = "url路径")
    private String url;
    @ApiModelProperty(value = "访问类型")
    private String method; // GET, POST, PUT, DELETE, ALL
}
