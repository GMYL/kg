package hb.kg.common.util.json.parser.deserializer;

import java.lang.reflect.Type;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONToken;

public class PropertyProcessableDeserializer implements ObjectDeserializer {
    public final Class<PropertyProcessable> type;

    public PropertyProcessableDeserializer(Class<PropertyProcessable> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type type,
                            Object fieldName) {
        PropertyProcessable processable;
        try {
            processable = this.type.newInstance();
        } catch (Exception e) {
            throw new JSONException("craete instance error");
        }
        Object object = parser.parse(processable, fieldName);
        return (T) object;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
