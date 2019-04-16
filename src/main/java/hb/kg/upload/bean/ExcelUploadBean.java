package hb.kg.upload.bean;

import org.springframework.data.mongodb.core.index.Indexed;

import hb.kg.common.bean.mongo.BaseMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Excel上传")
public abstract class ExcelUploadBean<T> extends BaseMgBean<T> {
    // 这两个东西是要存到数据库的，所以实现它们的子类必须覆盖它们
    @ApiModelProperty(value = "排序值")
    @Indexed
    protected Integer sequence; // 排序值
    @ApiModelProperty(value = "上传时excel的类型")
    @Indexed
    protected String excelType; // 上传时excel的类型

    public Integer getSequence() {
        return this.sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getExcelType() {
        return this.excelType;
    }

    public void setExcelType(String excelType) {
        this.excelType = excelType;
    }
}
