package hb.kg.wizard.bean.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点的不同类型，注意这是一个写死的数据，对应的还有非写死性数据
 */
@Getter
@AllArgsConstructor
public enum HBGraphNodeType {
    UNKNOWN(0, "unknown"), // 未知节点
    NORMAL(1, "normal"), // 普通节点
    WORD(2, "word"), // 单词
    LAW(3, "law"), // 法规
    LAWITEM(4, "lawitem"), // 条款
    ;
    private Integer index;
    private String name;

    public static HBGraphNodeType valueOfName(String name) {
        for (HBGraphNodeType serviceEnum : values()) {
            if (serviceEnum.getName().equals(name)) {
                return serviceEnum;
            }
        }
        return UNKNOWN;
    }

    public static HBGraphNodeType valueOf(int index) {
        for (HBGraphNodeType serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
