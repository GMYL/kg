package hb.kg.wizard.bean.enums;

/**
 * 词性枚举，注意并不是所有词性在这里都有列举，这里只出现最常用及有特殊意义的
 */
public enum HBGraphWordNature {
    NORMAL(0, "normal"), // 留空开头
    NCS(1, "ncs"); // 该词性对应税法名称
    private Integer index;
    private String name;

    private HBGraphWordNature(Integer index,
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

    public static HBGraphWordNature valueOf(int index) {
        for (HBGraphWordNature serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
