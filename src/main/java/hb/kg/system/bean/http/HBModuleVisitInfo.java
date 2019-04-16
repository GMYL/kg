package hb.kg.system.bean.http;

import org.springframework.data.annotation.Transient;

import hb.kg.common.util.json.anno.JSONField;
import hb.kg.system.bean.mongo.HBModule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统模块访问情况，但单独保存数据库，和系统信息保存到一起
 */
@ApiModel(description = "系统模块访问情况")
@Data
public class HBModuleVisitInfo {
    @ApiModelProperty(value = "和HBModule的id对应")
    private String id; // 和HBModule的id对应
    @ApiModelProperty(value = "和HBModule的name对应")
    private String name; // 和HBModule的name对应
    @ApiModelProperty(value = "访问数量")
    private Integer count; // 访问数量
    @ApiModelProperty(value = "来源的module", hidden = true)
    @Transient
    @JSONField(serialize = false)
    private HBModule srcModule; // 来源的module

    public HBModuleVisitInfo fromHBModule(HBModule module) {
        this.id = module.getId();
        this.name = module.getName();
        this.count = this.count == null ? 0 : this.count;
        this.srcModule = module;
        return this;
    }
}
