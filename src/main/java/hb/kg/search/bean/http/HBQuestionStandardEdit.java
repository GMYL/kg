package hb.kg.search.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.upload.bean.ExcelUploadBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_question_standards")
public class HBQuestionStandardEdit extends ExcelUploadBean<HBQuestionStandardEdit>
        implements Serializable {
    private static final long serialVersionUID = 1106649332573155699L;
    @Id
    private String id; // id
    private String type; // 类型
    private String title; // 问题描述
    private List<String> items; // 答案中包含的法规
    private Integer visitNum;
    private Date createTime; // 问题提交时间
}