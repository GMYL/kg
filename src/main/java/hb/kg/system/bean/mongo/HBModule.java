package hb.kg.system.bean.mongo;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import hb.kg.common.bean.auth.HBAuthURL;
import hb.kg.common.bean.mongo.BaseMgBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 每个module都是可以
 */
@ApiModel(description = "模块")
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_modules")
public class HBModule extends BaseMgBean<HBModule> implements Serializable {
    private static final long serialVersionUID = -1580843791657747151L;
    @ApiModelProperty(value = "模块ID")
    @Id
    private String id; // id
    @ApiModelProperty(value = "展示名称")
    private String name; // 展示名称
    @ApiModelProperty(value = "是否记录访问情况")
    private Boolean record; // 是否记录访问情况
    @ApiModelProperty(value = "模块分类")
    private String category; // 模块分类
    @ApiModelProperty(value = "url列表")
    private List<HBAuthURL> urls; // url-method(GET,POST,PUT,DELETE)，后台不需要做单独的url的库，这个复合对象强制完整存储后台
    @ApiModelProperty(value = "该模块的功能")
    private String feature; // 该模块的功能

    @Override
    public void prepareHBBean() {
        // urls = urls == null ? new ArrayList<>() : null;
        record = record == null ? false : true;
    }
}
