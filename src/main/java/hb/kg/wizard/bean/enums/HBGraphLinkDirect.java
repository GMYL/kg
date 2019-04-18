package hb.kg.wizard.bean.enums;

/**
 * 连接的不同方向
 */
public enum HBGraphLinkDirect {
    DIRECTED(0, "directed"), // 无方向的
    UNDIRECTED(1, "undirected"), // 有方向的
    BOTH(2, "both"), // 有无方向都可以
    ;
    private Integer index;
    private String name;

    private HBGraphLinkDirect(Integer index,
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

    public static HBGraphLinkDirect valueOf(int index) {
        for (HBGraphLinkDirect serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
