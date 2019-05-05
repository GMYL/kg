package hb.kg.law.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.upload.bean.ExcelUploadBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 条款库
 */
@ApiModel(description = "条款库")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_law_items")
public class HBLawitem extends ExcelUploadBean<HBLawitem> implements Serializable {
    private static final long serialVersionUID = -2103880728794810263L;
    @ApiModelProperty(value = "条款ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "条款")
    private String item; // 条款
    private String valid; // 条款有效性：前端展示

    public String toPattern() {
        return "#" + id + "#";
    }
}
