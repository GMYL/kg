package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.reflect.TypeUtils;

public class BigDecimalCodec implements ObjectSerializer, ObjectDeserializer {
    final static BigDecimal LOW = BigDecimal.valueOf(-9007199254740991L);
    final static BigDecimal HIGH = BigDecimal.valueOf(9007199254740991L);
    public final static BigDecimalCodec instance = new BigDecimalCodec();

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
            BigDecimal val = (BigDecimal) object;
            int scale = val.scale();
            String outText;
            if (out.isEnabled(SerializerFeature.WriteBigDecimalAsPlain) && scale >= -100
                    && scale < 100) {
                outText = val.toPlainString();
            } else {
                outText = val.toString();
            }
            if (scale == 0) {
                if (outText.length() >= 16
                        && SerializerFeature.isEnabled(features,
                                                       out.features,
                                                       SerializerFeature.BrowserCompatible)
                        && (val.compareTo(LOW) < 0 || val.compareTo(HIGH) > 0)) {
                    out.writeString(outText);
                    return;
                }
            }
            out.write(outText);
            if (out.isEnabled(SerializerFeature.WriteClassName) && fieldType != BigDecimal.class
                    && val.scale() == 0) {
                out.write('.');
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        try {
            return (T) deserialze(parser);
        } catch (Exception ex) {
            throw new JSONException("parseDecimal error, field : " + fieldName, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == JSONToken.LITERAL_INT) {
            BigDecimal decimalValue = lexer.decimalValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) decimalValue;
        }
        if (lexer.token() == JSONToken.LITERAL_FLOAT) {
            BigDecimal val = lexer.decimalValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val;
        }
        Object value = parser.parse();
        return value == null //
                ? null //
                : (T) TypeUtils.castToBigDecimal(value);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
