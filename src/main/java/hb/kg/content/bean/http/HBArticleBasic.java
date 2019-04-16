package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "文章")
@Data
@Document(collection = "hb_articles")
public class HBArticleBasic implements Serializable {
    private static final long serialVersionUID = 2508505606669276145L;
    @ApiModelProperty(value = "文章ID")
    @Id
    private String id;
    @ApiModelProperty(value = "文章标题")
    private String title;
    @ApiModelProperty(value = "文章标签")
    private List<String> categorys; // 文章标签
    @ApiModelProperty(value = "文章摘要")
    private String summary; // 文章摘要
    @ApiModelProperty(value = "文章点击次数")
    private Integer visitNum; // 文章点击次数
}
