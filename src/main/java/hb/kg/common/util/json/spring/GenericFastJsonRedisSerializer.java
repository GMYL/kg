package hb.kg.common.util.json.spring;

import org.apache.commons.lang.SerializationException;
import org.springframework.data.redis.serializer.RedisSerializer;

import hb.kg.common.util.file.IOUtils;
import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.parser.ParserConfig;
import hb.kg.common.util.json.serializer.SerializerFeature;

public class GenericFastJsonRedisSerializer implements RedisSerializer<Object> {
    private final static ParserConfig defaultRedisConfig = new ParserConfig();
    static {
        defaultRedisConfig.setAutoTypeSupport(true);
    }

    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, IOUtils.UTF8),
                                    Object.class,
                                    defaultRedisConfig);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
