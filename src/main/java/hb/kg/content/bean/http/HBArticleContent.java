package hb.kg.content.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "hb_articles")
public class HBArticleContent implements Serializable {
    private static final long serialVersionUID = -6290140006886482564L;
    @Id
    private String id;
    private String content;
}
