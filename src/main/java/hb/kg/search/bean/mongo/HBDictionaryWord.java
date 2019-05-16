package hb.kg.search.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.util.algo.nlp.trie.NlpWord;
import hb.kg.common.util.id.IDUtil;
import hb.kg.upload.bean.ExcelUploadBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分词词典中的词维护对象
 */
@ApiModel(description = "分词词典中的词维护对象")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_dictionaries")
public class HBDictionaryWord extends ExcelUploadBean<HBDictionaryWord> implements Serializable {
    private static final long serialVersionUID = 4556408758734318094L;
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
    @ApiModelProperty(value = "权重")
    private Double weight; // 权重
    @ApiModelProperty(value = "出现次数")
    private Integer occurNum; // 出现次数
    @ApiModelProperty(value = "近义词")
    private Map<String, Double> synonyms; // 近义词
    @ApiModelProperty(value = "词性")
    private String nature; // 词性

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        this.id = this.id == null ? IDUtil.generateRandomKey() : this.id;
        createTime = createTime == null ? new Date() : createTime;
        updateTime = updateTime == null ? new Date() : updateTime;
        weight = weight == null ? 1 : weight;
        occurNum = occurNum == null ? 0 : occurNum;
    }

    // 和nlp词互转
    public NlpWord toNlpWord() {
        NlpWord nlpWord = new NlpWord();
        nlpWord.setName(word);
        nlpWord.setNature(nature);
        nlpWord.setWeight(weight);
        return nlpWord;
    }
}
