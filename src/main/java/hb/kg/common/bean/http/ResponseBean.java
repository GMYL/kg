package hb.kg.common.bean.http;

import java.util.HashMap;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.util.set.HBCollectionUtil;
import lombok.Data;

@Data
public class ResponseBean {
    private String code = ApiCode.SUCCESS.getCode();
    private String errMsg = "";
    private Object data;
    private String stackMsg;

    /**
     * 如果data里只是一个keyvalue，这样就足够了
     */
    public void setOneData(String key,
                           Object value) {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(key, value);
        data = map;
    }

    public ResponseBean end() {
        if (data == null) {
            data = HBCollectionUtil.emptyList;
        }
        if (!ApiCode.SUCCESS.getCode().equals(code)) {
            errMsg += "\n若您操作无误，请联系网站管理人员";
        }
        return this;
    }

    public void setCodeAndErrMsg(String code,
                                 String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

    public void setCodeEnum(ApiCode code) {
        this.code = code.getCode();
        this.errMsg = code.getName();
    }
}
