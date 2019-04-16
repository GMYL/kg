package hb.kg.msg.bean.http;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;

import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HBSiteMailSome implements Serializable {
    private static final long serialVersionUID = 8421325724838915984L;
    private List<String> idList; // 接受者
    @DBRef
    private HBUserBasic from; // 发送者
    private String title; // 标题
    private String content; // 内容
}
