package hb.kg.search.bean.enums;

public enum HBEsIndexPrefix {
    ALL(0, "hb_all"), // 综合搜索
    ARTICLE(1, "hb_articles"), // 文章
    LAW(2, "hb_laws"), // 法规
    LAWITEM(3, "hb_law_items"), // 条款
    POLICY(3, "hb_policys"), // 政策解读
    QUESTION(4, "hb_questions"), // 问答
    TALK(5, "hb_talks"); // 名家税谈
    private Integer index;
    private String name;

    private HBEsIndexPrefix(Integer index,
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

    public static HBEsIndexPrefix valueOf(int index) {
        for (HBEsIndexPrefix serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
