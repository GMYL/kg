package hb.kg.search.bean.http;

import hb.kg.common.bean.mongo.Page;
import hb.kg.law.bean.mongo.HBLaw;
import lombok.Data;

@Data
public class HBLawKeyword {
    private String id; // id号
    private String nop; // 字号
    private String year; // 年度
    private String index; // 序号
    private String titleKeyword; // 标题关键字
    private String contentKeyword; // 正文关键字
    public Page<HBLaw> page; // 分页信息
}
