package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.reflect.TypeUtils;

public class LongCodec implements ObjectSerializer, ObjectDeserializer {
    public static LongCodec instance = new LongCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
        } else {
            long value = ((Long) object).longValue();
            out.writeLong(value);
            if (out.isEnabled(SerializerFeature.WriteClassName) //
                    && value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE //
                    && fieldType != Long.class && fieldType != long.class) {
                out.write('L');
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        Long longObject;
        try {
            final int token = lexer.token();
            if (token == JSONToken.LITERAL_INT) {
                long longValue = lexer.longValue();
                lexer.nextToken(JSONToken.COMMA);
                longObject = Long.valueOf(longValue);
            } else if (token == JSONToken.LITERAL_FLOAT) {
                BigDecimal number = lexer.decimalValue();
                longObject = TypeUtils.longValue(number);
                lexer.nextToken(JSONToken.COMMA);
            } else {
                if (token == JSONToken.LBRACE) {
                    JSONObject jsonObject = new JSONObject(true);
                    parser.parseObject(jsonObject);
                    longObject = TypeUtils.castToLong(jsonObject);
                } else {
                    Object value = parser.parse();
                    longObject = TypeUtils.castToLong(value);
                }
                if (longObject == null) {
                    return null;
                }
            }
        } catch (Exception ex) {
            throw new JSONException("parseLong error, field : " + fieldName, ex);
        }
        return clazz == AtomicLong.class //
                ? (T) new AtomicLong(longObject.longValue()) //
                : (T) longObject;
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
