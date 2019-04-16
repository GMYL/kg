package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 专题专栏基础数据
 */
@Data
@Document(collection = "hb_article_special")
public class HBArticleSpecialBasic implements Serializable {
    private static final long serialVersionUID = 3423984566362782474L;
    @Id
    private String id; // id号
    private String title; // 标题
    private Integer state; // 状态 //1.待审核 2.正常发布 3.历史
    private Date createTime; // 创建日期
    private Date updateTime; // 最后一次更新日期
    private Integer sequence;// 排序
}
