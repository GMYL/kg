package hb.kg.search.bean.http;

import java.io.Serializable;
import java.util.Map;

import hb.kg.common.bean.mongo.Page;
import lombok.Data;

/**
 * 搜索都用这个bean作为传入参数
 */
@Data
public class HBSearchBean implements Serializable {
    private static final long serialVersionUID = 8236309959388076795L;
    private String content;
    private Page<Map<String, Object>> page;
}
