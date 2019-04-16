package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;

public class ReferenceCodec implements ObjectSerializer, ObjectDeserializer {
    public final static ReferenceCodec instance = new ReferenceCodec();

    @SuppressWarnings("rawtypes")
    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        Object item;
        if (object instanceof AtomicReference) {
            AtomicReference val = (AtomicReference) object;
            item = val.get();
        } else {
            item = ((Reference) object).get();
        }
        serializer.write(item);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(DefaultJSONParser parser,
                            Type type,
                            Object fieldName) {
        ParameterizedType paramType = (ParameterizedType) type;
        Type itemType = paramType.getActualTypeArguments()[0];
        Object itemObject = parser.parseObject(itemType);
        Type rawType = paramType.getRawType();
        if (rawType == AtomicReference.class) {
            return (T) new AtomicReference(itemObject);
        }
        if (rawType == WeakReference.class) {
            return (T) new WeakReference(itemObject);
        }
        if (rawType == SoftReference.class) {
            return (T) new SoftReference(itemObject);
        }
        throw new UnsupportedOperationException(rawType.toString());
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
