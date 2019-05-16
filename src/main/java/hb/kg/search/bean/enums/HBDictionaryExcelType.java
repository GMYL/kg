package hb.kg.search.bean.enums;

/**
 * 上传单词对应的不同的类别
 */
public enum HBDictionaryExcelType {
    WENDA(0, "问答"),
    FAGUI(1, "法规"),
    WENZHANG(2, "文章");
    private Integer index;
    private String name;

    private HBDictionaryExcelType(Integer index,
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

    public static HBDictionaryExcelType valueOf(int index) {
        for (HBDictionaryExcelType serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
