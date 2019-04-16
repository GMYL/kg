package hb.kg.user.bean.enums;

/**
 * 表述用户特殊role的对应枚举
 */
public enum HBRoleEnum {
    /**
     * 花瓣员工
     */
    STAFF_ROLE(1, "role_staff");
    private Integer index;
    private String name;

    private HBRoleEnum(Integer index,
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

    public static HBRoleEnum valueOf(int index) {
        for (HBRoleEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
