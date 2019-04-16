package hb.kg.system.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "访问状态")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_system_visit_status")
public class HBVisitStatusBasic extends BaseMgBean<HBVisitStatusBasic> implements Serializable {
    private static final long serialVersionUID = 5468483370164442809L;
    @ApiModelProperty(value = "日志记录时间戳（如2018-08-01)")
    @Id
    private String id; // 日志记录时间戳（如2018-08-01)
    @ApiModelProperty(value = "日志等级：minute、hour、day")
    private String period; // 日志等级：minute、hour、day
    @ApiModelProperty(value = "该周期内用户访问总数")
    private Integer count; // 该周期内用户访问总数
    @ApiModelProperty(value = "查询的起始时间")
    @Transient
    private Date timeStart; // 查询的起始时间
    @ApiModelProperty(value = "查询的起始终止")
    @Transient
    private Date timeEnd; // 查询的起始终止
}
