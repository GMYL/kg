package hb.kg.search.bean.http;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "标准问答")
@Data
@Document(collection = "hb_question_standards")
public class HBQuestionStandardBasic {
    @ApiModelProperty(value = "问答ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "问题描述")
    private String title; // 问题描述
    @ApiModelProperty(value = "问答正文")
    private String content; // 问答正文
    @ApiModelProperty(value = "问题被问到的次数")
    private Integer visitNum; // 问题被问到的次数
}
