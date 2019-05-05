package hb.kg.law.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import hb.kg.law.bean.http.HBLawBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_laws")
public class HBLaw extends BaseMgBean<HBLaw> implements Serializable {
    private static final long serialVersionUID = -9087214526010481847L;
    @Id
    private String id; // 法规id号
    private String name; // 法规名称
    private String no; // 法规号
    private Date date; // 法规发布日期
    private String from; // 法规原文
    private String alias; // 别名
    private String source; // 文章来源
    private String valid; // 法规有效性：前端展示
    private String basics;// 法规基础性
    private String gist; // 失效依据
    private String pictures; // 图片集
    private Boolean state; // 法规有效性，后台判断
    private String contents; // 法规正文
    private Integer visitNum; // 点击量
    private String excelType; // 上传时excel的类型
    @DBRef
    private List<HBLawBasic> links; // 文内链接
    private Map<String, String> linkReplaces; // 文内链接替换项
    private List<String> attaches; // 相关文件
    private List<String> annexes; // 附件
    @Transient
    private List<HBLaw> attachesList; // 相关文件
    @Transient
    private List<String> repeats; // 和本法规重复的法规id号

    @Override
    public void prepareHBBean() {
        id = StringUtils.isNotEmpty(id) ? id : IDUtil.generateRandomKey(9); // 如果id号不存在，使用随机9位码实现id号注入
        visitNum = visitNum != null ? visitNum : 0;
        state = state != null ? state : true;
    }
}
