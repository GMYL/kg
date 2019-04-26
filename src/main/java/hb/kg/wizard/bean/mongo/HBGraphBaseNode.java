package hb.kg.wizard.bean.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.id.IDUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 所有节点的基类对象，这个类需要被其它类实现，本身不做存储，提高访问效率
// 对于所有的node，尽量使用规范统一的存储方式，不再额外开辟空间或是其他的东西
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_graph_nodes")
public class HBGraphBaseNode extends BaseMgBean<HBGraphBaseNode> {
    @Id
    private String id;
    private Double weight; // 权重
    private String type; // 节点类型，对应HBGraphWordType
    // 注意词本身的连接性关系不会在词内部进行保存
    // 关系单独保存，关系内部对词进行一次索引，保证查询的时候按照关系的全局索引查询即可
    // 也就是说，每个节点是独立的，节点的存储本身不涉及边的存储
    
    @Transient
    private String word; // 实体
    @Transient
    private String nature;// 属性

    // 节点的id号不再自动生成，必须手动生成
    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        id = id == null ? IDUtil.generateRandomKey() : id;
        weight = weight == null ? 1.0 : weight;
    }

    /**
     * 将本对象转变为它的一个子类，转变之后会生成一个新的对象，可以在子类中覆盖本方法实现自己的方法
     */
    public static <T extends HBGraphBaseNode> T fromBaseNode(HBGraphBaseNode src,
                                                             Class<T> tarClazz)
            throws InstantiationException, IllegalAccessException {
        T newObj = tarClazz.newInstance();
        newObj.setId(src.getId());
        newObj.setWeight(src.getWeight());
        newObj.setType(src.getType());
        return newObj;
    }
}
