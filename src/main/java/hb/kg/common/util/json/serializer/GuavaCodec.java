package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.common.collect.Multimap;

public class GuavaCodec implements ObjectSerializer {
    public static GuavaCodec instance = new GuavaCodec();

    @SuppressWarnings("rawtypes")
    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        if (object instanceof Multimap) {
            Multimap multimap = (Multimap) object;
            serializer.write(multimap.asMap());
        }
    }
}
