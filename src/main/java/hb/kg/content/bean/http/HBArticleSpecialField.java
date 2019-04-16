package hb.kg.content.bean.http;

import lombok.Data;

@Data
public class HBArticleSpecialField {
    private String id;// 文章或法规的id
    private String title;// 标题
    private String date;// 发文时间

    public HBArticleSpecialField() {
    }

    public HBArticleSpecialField(String id,
                                 String title,
                                 String date) {
        setId(id);
        setTitle(title);
        setDate(date);
    }
}
