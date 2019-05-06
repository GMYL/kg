package hb.kg.wizard.bean.enums;

/**
 * 连接的不同类型，用于扩展的时候，有的属性使能，有的不使能
 */
public enum HBGraphLinkType {
    SYNONYM(0, "synonym"), // 近义关系
    PARENT(1, "parent"), // 父子关系
    ATTRIBUTE(2, "attribute"), // 属性关系
    CONTENT(3, "content"), // 来自正文的描述
    TYPE(4, "type"), // 分类
    ITEM(5, "item"), // 条款
    ;
    private Integer index;
    private String name;

    private HBGraphLinkType(Integer index,
                            String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static HBGraphLinkType valueOf(int index) {
        for (HBGraphLinkType serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
