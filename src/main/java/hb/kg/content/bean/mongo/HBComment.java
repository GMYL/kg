package hb.kg.content.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "评论基本信息")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_comments")
public class HBComment extends BaseMgBean<HBComment> implements Serializable {
    private static final long serialVersionUID = -1011434281807182793L;
    @ApiModelProperty(value = "评论ID")
    @Id
    private String id;
    @ApiModelProperty(value = "作者名称")
    private String author; // 作者名称
    @ApiModelProperty(value = "评论日期")
    private Date uploadTime; // 评论日期
    @ApiModelProperty(value = "评论状态")
    private boolean state; // 评论状态
}
