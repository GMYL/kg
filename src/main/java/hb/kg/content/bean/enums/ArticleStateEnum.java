package hb.kg.content.bean.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStateEnum {
    DRAFT(0, "draft"), // 草稿
    AUDIT(1, "audit"), // 待审
    POSTED(2, "posted"), // 已发布
    DELETE(3, "delete"), // 已删除
    ;
    private Integer index;
    private String name;

    public static ArticleStateEnum valueOf(int index) {
        for (ArticleStateEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
