package hb.kg.upload.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 有些时候，我们需要在mongo中存一部分会很快删掉的数据 这个类就用于这种场景下，把真正的bean先包装起来，等需要的时候再存到mongo中
 */
@Data
@Document(collection = "hb_temp")
@EqualsAndHashCode(callSuper = false)
public class HBTempMongoBean extends BaseMgBean<HBTempMongoBean> implements Serializable {
    private static final long serialVersionUID = 4080477301794825709L;
    @Id
    private String id; // ID
    private Object srcBean; // 对象
    private String clazz; // src对象的类
    private Date createTime; // 上传时间
    @DBRef
    private HBUserBasic user; // 上传人
    private String excelType; // 上传对象的类型

    @Override
    public void prepareHBBean() {
        this.id = this.id != null ? this.id : RandomStringUtils.random(9, true, true);
        this.createTime = this.createTime != null ? this.createTime : new Date();
        this.excelType = this.excelType != null ? this.excelType : "无";
    }
}
