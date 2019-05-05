package hb.kg.wizard.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.encrypt.MD5Util;
import hb.kg.common.util.id.IDUtil;
import hb.kg.wizard.bean.enums.HBGraphLinkDirect;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 两个节点之间的连线，注意一定是有方向性的，会有startNode和endNode。如果本身是无方向性的，那么startNode和endNode也可以按照这个思路配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_graph_links")
public class HBGraphLink extends BaseMgBean<HBGraphLink> implements Serializable {
    private static final long serialVersionUID = -353603484017662051L;
    @Id
    private String id;
    @Indexed
    private Integer direct; // 连接方向，对应HBGraphLinkDirect
    @Indexed
    private String type; // 连接类型，对应HBGraphLinkType
    private Double weight; // 连接权重
    @Indexed
    private String start;
    @Indexed
    private String end;
    @Indexed
    private String encrypt;// start和end的加密
    private String nature;// 属性

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        id = id == null ? IDUtil.generateRandomKey() : id;
        encrypt = encrypt == null ? MD5Util.getRandomMD5Code(start + end) : encrypt;
        direct = direct == null ? HBGraphLinkDirect.DIRECTED.getIndex() : direct;
        weight = weight == null ? 1.0 : weight;
    }

    public String generateKey() {
        return start + "-" + end;
    }

    public String generateKeyReverse() {
        return end + "-" + start;
    }
}
