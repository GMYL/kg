package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import hb.kg.common.util.json.JSONAware;

public class JSONAwareSerializer implements ObjectSerializer {
    public static JSONAwareSerializer instance = new JSONAwareSerializer();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        JSONAware aware = (JSONAware) object;
        out.write(aware.toJSONString());
    }
}
