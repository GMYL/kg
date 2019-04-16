package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;

public class AtomicCodec implements ObjectSerializer, ObjectDeserializer {
    public final static AtomicCodec instance = new AtomicCodec();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object instanceof AtomicInteger) {
            AtomicInteger val = (AtomicInteger) object;
            out.writeInt(val.get());
            return;
        }
        if (object instanceof AtomicLong) {
            AtomicLong val = (AtomicLong) object;
            out.writeLong(val.get());
            return;
        }
        if (object instanceof AtomicBoolean) {
            AtomicBoolean val = (AtomicBoolean) object;
            out.append(val.get() ? "true" : "false");
            return;
        }
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        if (object instanceof AtomicIntegerArray) {
            AtomicIntegerArray array = (AtomicIntegerArray) object;
            int len = array.length();
            out.write('[');
            for (int i = 0; i < len; ++i) {
                int val = array.get(i);
                if (i != 0) {
                    out.write(',');
                }
                out.writeInt(val);
            }
            out.write(']');
            return;
        }
        AtomicLongArray array = (AtomicLongArray) object;
        int len = array.length();
        out.write('[');
        for (int i = 0; i < len; ++i) {
            long val = array.get(i);
            if (i != 0) {
                out.write(',');
            }
            out.writeLong(val);
        }
        out.write(']');
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type clazz,
                            Object fieldName) {
        if (parser.lexer.token() == JSONToken.NULL) {
            parser.lexer.nextToken(JSONToken.COMMA);
            return null;
        }
        JSONArray array = new JSONArray();
        parser.parseArray(array);
        if (clazz == AtomicIntegerArray.class) {
            AtomicIntegerArray atomicArray = new AtomicIntegerArray(array.size());
            for (int i = 0; i < array.size(); ++i) {
                atomicArray.set(i, array.getInteger(i));
            }
            return (T) atomicArray;
        }
        AtomicLongArray atomicArray = new AtomicLongArray(array.size());
        for (int i = 0; i < array.size(); ++i) {
            atomicArray.set(i, array.getLong(i));
        }
        return (T) atomicArray;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACKET;
    }
}
