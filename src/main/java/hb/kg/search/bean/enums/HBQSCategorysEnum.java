package hb.kg.search.bean.enums;

/**
 * 标准问答分类
 * @author GMY
 */
public enum HBQSCategorysEnum {
    HB_MASTER(1, "主站"), // 主站
    HB_REALTY(2, "房地产平台"), // 房地产平台
    HB_TRADING(3, "进出口平台"); // 进出口平台
    private Integer index;
    private String name;

    private HBQSCategorysEnum(Integer index,
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

    public static HBQSCategorysEnum valueOf(int index) {
        for (HBQSCategorysEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
