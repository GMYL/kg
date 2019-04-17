package hb.kg.law.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "法规")
@Data
@Document(collection = "hb_laws")
public class HBLawBasicEver implements Serializable {
    private static final long serialVersionUID = 6067032623096855997L;
    @ApiModelProperty(value = "法规ID")
    @Id
    private String id;
    @ApiModelProperty(value = "法规名称")
    private String name; // 法规名称
    @ApiModelProperty(value = "法规号")
    private String no; // 法规号
}