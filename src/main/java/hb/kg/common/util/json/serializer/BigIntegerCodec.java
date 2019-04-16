package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.reflect.TypeUtils;

public class BigIntegerCodec implements ObjectSerializer, ObjectDeserializer {
    private final static BigInteger LOW = BigInteger.valueOf(-9007199254740991L);
    private final static BigInteger HIGH = BigInteger.valueOf(9007199254740991L);
    public final static BigIntegerCodec instance = new BigIntegerCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        BigInteger val = (BigInteger) object;
        String str = val.toString();
        if (str.length() >= 16
                && SerializerFeature.isEnabled(features,
                                               out.features,
                                               SerializerFeature.BrowserCompatible)
                && (val.compareTo(LOW) < 0 || val.compareTo(HIGH) > 0)) {
            out.writeString(str);
            return;
        }
        out.write(str);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == JSONToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(JSONToken.COMMA);
            return (T) new BigInteger(val);
        }
        Object value = parser.parse();
        return value == null //
                ? null //
                : (T) TypeUtils.castToBigInteger(value);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
