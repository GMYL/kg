package hb.kg.wizard.bean.mongo;

import java.io.Serializable;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.wizard.bean.enums.HBGraphNodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 法规
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_graph_nodes")
public class HBGraphLaw extends HBGraphBaseNode implements Serializable {
    private static final long serialVersionUID = -8422125478393149899L;
    @Indexed
    private String word; // 实体
    private String nature;// 属性

    public HBGraphLaw() {
        super();
    }

    public HBGraphLaw(String id) {
        super();
        setId(id);
    }

    /**
     * 将法规库的法规转变为知识图谱的法规，ID号规则是随机
     */
    public static HBGraphLaw genGraphLaw(HBLaw src,
                                         String nature) {
        HBGraphLaw law = new HBGraphLaw();
        // law.setWord(src.getName());
        law.setNature(nature);
        law.setType(HBGraphNodeType.LAW.getName());
        law.setWeight(1.0);
        law.prepareHBBean();
        return law;
    }

    public static HBGraphLaw fromBaseNode(HBGraphBaseNode src)
            throws InstantiationException, IllegalAccessException {
        HBGraphLaw newObj = new HBGraphLaw();
        newObj.setId(src.getId());
        newObj.setWeight(src.getWeight());
        newObj.setType(src.getType());
        newObj.setWord(src.getWord());
        newObj.setNature(src.getNature());
        return newObj;
    }
}
