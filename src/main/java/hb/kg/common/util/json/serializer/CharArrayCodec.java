package hb.kg.common.util.json.serializer;

import java.lang.reflect.Type;
import java.util.Collection;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;

public class CharArrayCodec implements ObjectDeserializer {
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            String val = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val.toCharArray();
        }
        if (lexer.token() == JSONToken.LITERAL_INT) {
            Number val = lexer.integerValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val.toString().toCharArray();
        }
        Object value = parser.parse();
        if (value instanceof String) {
            return (T) ((String) value).toCharArray();
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            boolean accept = true;
            for (Object item : collection) {
                if (item instanceof String) {
                    int itemLength = ((String) item).length();
                    if (itemLength != 1) {
                        accept = false;
                        break;
                    }
                }
            }
            if (!accept) {
                throw new JSONException("can not cast to char[]");
            }
            char[] chars = new char[collection.size()];
            int pos = 0;
            for (Object item : collection) {
                chars[pos++] = ((String) item).charAt(0);
            }
            return (T) chars;
        }
        return value == null //
                ? null //
                : (T) JSON.toJSONString(value).toCharArray();
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
