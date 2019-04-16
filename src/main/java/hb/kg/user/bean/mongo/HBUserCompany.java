package hb.kg.user.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户所属公司或组织
 */
@ApiModel(description = " 用户所属公司或组织")
@Data
@Document(collection = "hb_user_companys")
public class HBUserCompany implements Serializable {
    private static final long serialVersionUID = -6245064290319927929L;
    @ApiModelProperty(value = "公司id号，和名称保持一致")
    @Id
    private String id; // 公司id号，和名称保持一致
    @ApiModelProperty(value = "公司简介")
    private String introduction; // 公司简介
}
