package hb.kg.system.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseTreeMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 每个module都是可以
 */
@ApiModel(description = "模块")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_modules_categorys")
public class HBModuleCategory extends BaseTreeMgBean<HBModuleCategory> implements Serializable {
    private static final long serialVersionUID = -1580843791657747151L;
    @ApiModelProperty(value = "模块ID")
    @Id
    private String id; // id
}
