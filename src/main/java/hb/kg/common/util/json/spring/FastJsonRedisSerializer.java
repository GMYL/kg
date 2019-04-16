package hb.kg.common.util.json.spring;

import org.apache.commons.lang3.SerializationException;
import org.springframework.data.redis.serializer.RedisSerializer;

import hb.kg.common.util.json.FastJsonConfig;
import hb.kg.common.util.json.JSON;

public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();
    private Class<T> type;

    public FastJsonRedisSerializer(Class<T> type) {
        this.type = type;
    }

    public FastJsonConfig getFastJsonConfig() {
        return fastJsonConfig;
    }

    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(fastJsonConfig.getCharset(),
                                    t,
                                    fastJsonConfig.getSerializeConfig(),
                                    fastJsonConfig.getSerializeFilters(),
                                    fastJsonConfig.getDateFormat(),
                                    JSON.DEFAULT_GENERATE_FEATURE,
                                    fastJsonConfig.getSerializerFeatures());
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) JSON.parseObject(bytes,
                                        fastJsonConfig.getCharset(),
                                        type,
                                        fastJsonConfig.getParserConfig(),
                                        fastJsonConfig.getParseProcess(),
                                        JSON.DEFAULT_PARSER_FEATURE,
                                        fastJsonConfig.getFeatures());
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
