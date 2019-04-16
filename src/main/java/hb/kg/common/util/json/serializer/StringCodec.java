package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;

public class StringCodec implements ObjectSerializer, ObjectDeserializer {
    public static StringCodec instance = new StringCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        write(serializer, (String) object);
    }

    public void write(JSONSerializer serializer,
                      String value) {
        SerializeWriter out = serializer.out;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
            return;
        }
        out.writeString(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        if (clazz == StringBuffer.class) {
            final JSONLexer lexer = parser.lexer;
            if (lexer.token() == JSONToken.LITERAL_STRING) {
                String val = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);
                return (T) new StringBuffer(val);
            }
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) new StringBuffer(value.toString());
        }
        if (clazz == StringBuilder.class) {
            final JSONLexer lexer = parser.lexer;
            if (lexer.token() == JSONToken.LITERAL_STRING) {
                String val = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);
                return (T) new StringBuilder(val);
            }
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) new StringBuilder(value.toString());
        }
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            String val = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val;
        }
        if (lexer.token() == JSONToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val;
        }
        Object value = parser.parse();
        if (value == null) {
            return null;
        }
        return (T) value.toString();
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
