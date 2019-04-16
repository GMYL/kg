package hb.kg.common.util.json.parser.deserializer;

import java.lang.reflect.Type;

public interface FieldTypeResolver extends ParseProcess {
    Type resolve(Object object,
                 String fieldName);
}
