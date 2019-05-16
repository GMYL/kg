package hb.kg.search.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.util.id.IDUtil;
import hb.kg.common.util.set.HBCollectionUtil;
import hb.kg.law.bean.http.HBLawBasicEver;
import hb.kg.search.bean.enums.HBQSCategorysEnum;
import hb.kg.upload.bean.ExcelUploadBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准问题库问题
 */
@ApiModel(description = "标准问题库问题")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_question_standards")
public class HBQuestionStandard extends ExcelUploadBean<HBQuestionStandard>
        implements Serializable {
    private static final long serialVersionUID = -4189412184746469565L;
    @ApiModelProperty(value = "ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "类型")
    private String type; // 类型
    @ApiModelProperty(value = "问题描述")
    private String title; // 问题描述
    @ApiModelProperty(value = "暗标题")
    private String darkTitle; // 暗标题
    @ApiModelProperty(value = "问题答案正文")
    private String content; // 问题答案正文
    @ApiModelProperty(value = "答案中包含的法规")
    private List<String> items; // 答案中包含的法规
    @ApiModelProperty(value = "问题被问到的次数")
    private Integer visitNum; // 问题被问到的次数
    @ApiModelProperty(value = "问题提交时间")
    private Date createTime; // 问题提交时间
    @ApiModelProperty(value = "问题当前是否有效")
    private Boolean valid; // 问题当前是否有效
    private List<String> categorys; // 分类 主站、房地产子站
    @ApiModelProperty(value = "附属法规")
    @Transient
    private HashMap<String, String> lawitems; // 这个数据库不存，每次都重新填充
    @ApiModelProperty(value = "分数")
    @Transient
    private Double score; // 这个数据库不存，每次都重新填充
    @ApiModelProperty(value = "归一化分数")
    @Transient
    private Double scoreFlat; // 这个数据库不存，每次都重新填充
    @ApiModelProperty(value = "从属法规", hidden = true)
    @Transient
    private HashMap<String, HBLawBasicEver> lawitemBelongs; // 这个数据库不存，每次都重新填充
    @ApiModelProperty(value = "用户是否关注")
    @Transient
    private Boolean focus; // 用户是否关注gmy

    public void prepareForGet() {
        this.focus = this.focus != null ? this.focus : false;
    }

    public HBQuestionStandard() {}


    @Override
    public void prepareHBBean() {
        id = id != null ? id : "QA" + IDUtil.generateTimedIDStr();
        type = type != null ? type : "spec";
        visitNum = 0;
        createTime = new Date();
        valid = valid != null ? valid : true;
        categorys = categorys != null ? categorys
                : HBCollectionUtil.stringToList(HBQSCategorysEnum.HB_MASTER.getName());
    }
}
