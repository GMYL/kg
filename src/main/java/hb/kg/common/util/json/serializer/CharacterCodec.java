package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.reflect.TypeUtils;

public class CharacterCodec implements ObjectSerializer, ObjectDeserializer {
    public final static CharacterCodec instance = new CharacterCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        Character value = (Character) object;
        if (value == null) {
            out.writeString("");
            return;
        }
        char c = value.charValue();
        if (c == 0) {
            out.writeString("\u0000");
        } else {
            out.writeString(value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        Object value = parser.parse();
        return value == null //
                ? null //
                : (T) TypeUtils.castToChar(value);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
