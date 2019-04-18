package hb.kg.wizard.bean.http;

import java.util.Collection;

import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import hb.kg.wizard.bean.mongo.HBGraphLink;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 传到前端用来让echarts进行显示用的类
 */
@Data
@AllArgsConstructor
public class HBGraphWordWithLink {
    private Collection<HBGraphBaseNode> data;
    private Collection<HBGraphLink> links;
}
