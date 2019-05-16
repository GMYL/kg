package hb.kg.search.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 生成mapDarkTitle
 * @author GMY
 */
@Data
@Document(collection = "hb_question_standards2")
public class HBQuestionStandardMapTitle implements Serializable {
    private static final long serialVersionUID = 8890743936116466142L;
    @Id
    private String id;
    private String title; // 问题描述
    private List<HBDarkTitle> mapDarkTitle; // map阴标题
}
