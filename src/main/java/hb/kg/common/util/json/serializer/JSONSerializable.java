package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public interface JSONSerializable {
    /**
     * write to json
     * @param serializer json seriliazer
     * @param fieldName field name
     * @param fieldType field type
     * @param features field features
     * @throws IOException
     */
    void write(JSONSerializer serializer, //
               Object fieldName, //
               Type fieldType, //
               int features //
    ) throws IOException;
}
