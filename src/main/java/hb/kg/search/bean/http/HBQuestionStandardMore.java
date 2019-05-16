package hb.kg.search.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.upload.bean.ExcelUploadBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准问题库问题有更多附加内容
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_question_standards")
public class HBQuestionStandardMore extends ExcelUploadBean<HBQuestionStandardMore>
        implements Serializable {
    private static final long serialVersionUID = -4189412184746469565L;
    @Id
    private String id; // id
    private String type; // 类型
    private String title; // 问题描述
    private String content; // 问题答案正文
    private List<String> items; // 答案中包含的法规
    private Integer visitNum;
    private Date createTime; // 问题提交时间
    private Boolean valid; // 问题当前是否有效
    @Transient
    private HashMap<String, String> lawitems; // 这个数据库不存，每次都重新填充
    private Boolean focus; // 用户是否关注

    public void prepareForGet() {
        this.focus = this.focus != null ? this.focus : false;
    }
}
