package hb.kg.common.util.json.parser.deserializer;

import java.lang.reflect.Type;

import hb.kg.common.util.json.parser.DefaultJSONParser;

public interface ObjectDeserializer {
    <T> T deserialze(DefaultJSONParser parser,
                     Type type,
                     Object fieldName);

    int getFastMatchToken();
}
