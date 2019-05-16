package hb.kg.search.bean.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.law.bean.http.HBLawBasicEver;
import hb.kg.law.bean.mongo.HBLawitem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "标准问答")
@Data
@Document(collection = "hb_question_standards")
public class HBQuestionStandardFocus implements Serializable {
    private static final long serialVersionUID = -4115802219901406641L;
    @ApiModelProperty(value = "标准问答ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "问题描述")
    private String title; // 问题描述
    @ApiModelProperty(value = "问答正文")
    private String content; // 问答正文
    @ApiModelProperty(value = "答案中包含的法规")
    private List<String> items; // 答案中包含的法规
    @ApiModelProperty(value = "附属法规", hidden = true)
    @Transient
    private HashMap<String, HBLawBasicEver> lawitemBelongs; // 这个数据库不存，每次都重新填充
    @ApiModelProperty(value = "条款", hidden = true)
    @Transient
    private HashMap<String, HBLawitem> lawitems; // 这个数据库不存，每次都重新填充
}
