package hb.kg.search.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.util.id.IDUtil;
import hb.kg.upload.bean.ExcelUploadBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停止词词典中的词维护对象
 */
@ApiModel(description = "停止词词典中的词维护对象")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_dictionaries_stopwords")
public class HBDictionaryStopWord extends ExcelUploadBean<HBDictionaryStopWord>
        implements Serializable {
    private static final long serialVersionUID = 8884427238593388294L;
    @ApiModelProperty(value = "ID")
    @Id
    private String id;
    // @Indexed
    // protected String excelType; // 继承ExcelUploadBean里的exclType字段记录问答、法规、文章
    @ApiModelProperty(value = "全词")
    @Indexed
    private String word; // 全词
    @ApiModelProperty(value = "创建时间")
    private Date createTime; // 创建时间
    @ApiModelProperty(value = "修改时间")
    private Date updateTime; // 修改时间

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        this.id = this.id == null ? IDUtil.generateRandomKey() : this.id;
        createTime = createTime == null ? new Date() : createTime;
        updateTime = updateTime == null ? new Date() : updateTime;
    }
}
