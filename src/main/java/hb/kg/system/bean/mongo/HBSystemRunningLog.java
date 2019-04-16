package hb.kg.system.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 记录系统的每次定时性任务运行的概况
 */
@ApiModel(description = "系统运行时日志")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_system_running_logs")
public class HBSystemRunningLog extends BaseMgBean<HBSystemRunningLog> implements Serializable {
    private static final long serialVersionUID = -1791804362088425803L;
    @ApiModelProperty(value = "ID")
    @Id
    private String id;
    @ApiModelProperty(value = "日志发生时间")
    private Date createTime; // 日志发生时间
    @ApiModelProperty(value = "日志类型（填充发生日志的类）")
    private String type; // 日志类型（填充发生日志的类）
    @ApiModelProperty(value = "日志内容")
    private String content; // 日志内容
    @ApiModelProperty(value = "查询的起始时间")
    @Transient
    private Date timeStart; // 查询的起始时间
    @ApiModelProperty(value = "查询的起始终止")
    @Transient
    private Date timeEnd; // 查询的起始终止

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        this.id = StringUtils.isNotBlank(id) ? this.id : IDUtil.generateTimedIDStr();
        this.createTime = this.createTime != null ? this.createTime : new Date();
    }
}
