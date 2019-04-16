package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.reflect.TypeUtils;

public class CollectionCodec implements ObjectSerializer, ObjectDeserializer {
    public final static CollectionCodec instance = new CollectionCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        Type elementType = null;
        if (out.isEnabled(SerializerFeature.WriteClassName)
                || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName)) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        Collection<?> collection = (Collection<?>) object;
        SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            if (HashSet.class == collection.getClass()) {
                out.append("Set");
            } else if (TreeSet.class == collection.getClass()) {
                out.append("TreeSet");
            }
        }
        try {
            int i = 0;
            out.append('[');
            for (Object item : collection) {
                if (i++ != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.writeNull();
                    continue;
                }
                Class<?> clazz = item.getClass();
                if (clazz == Integer.class) {
                    out.writeInt(((Integer) item).intValue());
                    continue;
                }
                if (clazz == Long.class) {
                    out.writeLong(((Long) item).longValue());
                    if (out.isEnabled(SerializerFeature.WriteClassName)) {
                        out.write('L');
                    }
                    continue;
                }
                ObjectSerializer itemSerializer = serializer.getObjectWriter(clazz);
                if (SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName)
                        && itemSerializer instanceof JavaBeanSerializer) {
                    JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) itemSerializer;
                    javaBeanSerializer.writeNoneASM(serializer, item, i - 1, elementType, features);
                } else {
                    itemSerializer.write(serializer, item, i - 1, elementType, features);
                }
            }
            out.append(']');
        } finally {
            serializer.context = context;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(DefaultJSONParser parser,
                            Type type,
                            Object fieldName) {
        if (parser.lexer.token() == JSONToken.NULL) {
            parser.lexer.nextToken(JSONToken.COMMA);
            return null;
        }
        if (type == JSONArray.class) {
            JSONArray array = new JSONArray();
            parser.parseArray(array);
            return (T) array;
        }
        Collection list = TypeUtils.createCollection(type);
        Type itemType = TypeUtils.getCollectionItemType(type);
        parser.parseArray(itemType, list, fieldName);
        return (T) list;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACKET;
    }
}
