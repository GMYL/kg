package hb.kg.common.util.json.spring;

import java.io.IOException;
import java.lang.reflect.Type;

import hb.kg.common.util.json.serializer.JSONSerializer;
import hb.kg.common.util.json.serializer.ObjectSerializer;
import hb.kg.common.util.json.serializer.SerializeWriter;
import springfox.documentation.spring.web.json.Json;

/**
 * Swagger的Json处理，解决/v2/api-docs获取不到内容导致获取不到API页面内容的问题
 * @INFO 注意这里我们必须导入swagger的包，同时意味着如果不用swagger务必在配置文件中将swagger.enable设置为false
 */
public class SwaggerJsonSerializer implements ObjectSerializer {
    public final static SwaggerJsonSerializer instance = new SwaggerJsonSerializer();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.getWriter();
        Json json = (Json) object;
        String value = json.value();
        out.write(value);
    }
}
