package hb.kg.law.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "法规基本信息")
@Data
@Document(collection = "hb_laws")
public class HBLawBasic implements Serializable {
	private static final long serialVersionUID = 6067032623096855997L;
	@ApiModelProperty(value = "法规ID")
	@Id
	private String id;
	@ApiModelProperty(value = "法规名称")
	private String name; // 法规名称
	@ApiModelProperty(value = "法规号")
	private String no; // 法规号
	@ApiModelProperty(value = "法规发布日期")
	private Date date; // 法规发布日期
	@ApiModelProperty(value = "法规原文")
	private String from; // 法规原文
	@ApiModelProperty(value = "法规正文")
	private String contents; // 法规正文
	@ApiModelProperty(value = "法规有效性")
	private String valid;// 法规有效性
	@ApiModelProperty(value = "法规基础性")
	private String basics; // 法规基础性
	@ApiModelProperty(value = "法规有效性")
	private Boolean state;// 法规有效性
	@ApiModelProperty(value = "排序号")
	private Integer sequence; // 排序号
	@ApiModelProperty(value = "上传时excel的类型")
	private String excelType; // 上传时excel的类型
}