package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "文章标签")
@Data
@Document(collection = "hb_articles")
public class HBArticleCategorys implements Serializable {
    private static final long serialVersionUID = 2031226730346730900L;
    @Id
    private String id;
    @ApiModelProperty(value = "文章标签")
    private List<String> categorys; // 文章标签
}
