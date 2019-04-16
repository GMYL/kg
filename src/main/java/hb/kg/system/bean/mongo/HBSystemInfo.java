package hb.kg.system.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.system.bean.http.HBModuleVisitInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "系统信息")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_system_infos")
public class HBSystemInfo extends BaseMgBean<HBSystemInfo> implements Serializable {
    private static final long serialVersionUID = -8734908485473512381L;
    @ApiModelProperty(value = "数据时间")
    @Id
    private String id; // 数据时间
    @ApiModelProperty(value = "类型")
    private String period; // 类型（minute、hour、day）
    @ApiModelProperty(value = "实际采集时间")
    private Date createTime; // 实际采集时间
    @ApiModelProperty(value = "当前线程数")
    private int threadNum; // 当前线程数
    @ApiModelProperty(value = "jobqueue的长度")
    private int jobQueueSize; // jobqueue的长度
    @ApiModelProperty(value = "各个组件的状态", hidden = true)
    private List<HBModuleVisitInfo> moduleVisit; // 各个组件的状态
    @ApiModelProperty(value = "查询的起始时间")
    @Transient
    private Date timeStart; // 查询的起始时间
    @ApiModelProperty(value = "查询的起始终止")
    @Transient
    private Date timeEnd; // 查询的起始终止
}
