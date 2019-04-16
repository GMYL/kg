package hb.kg.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 获取用户vip信息
 */
@Data
@Document(collection = "hb_users")
public class HBUserTypeHttp implements Serializable {
    private static final long serialVersionUID = -4417869859901552141L;
    @Id
    private String id; // 用户id
    private Integer vip;// 用户vip登记
    private Boolean valid; // 用户是否有效
}
