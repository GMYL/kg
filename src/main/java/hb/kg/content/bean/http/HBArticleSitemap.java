package hb.kg.content.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * sitemap生成用到
 * @author GMY
 */
@Data
@Document(collection = "hb_articles")
public class HBArticleSitemap implements Serializable {
    private static final long serialVersionUID = -6290140006886482564L;
    @Id
    private String id;
    private String title;
    private String content;
    private List<String> categorys;
}
