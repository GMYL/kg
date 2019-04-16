package hb.kg.common.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import hb.kg.common.bean.mongo.BaseIdBean;
import hb.kg.common.server.MainServer;
import hb.kg.common.util.json.spring.FastJsonRedisSerializer;
import lombok.Getter;

/**
 * 在非mongo环境下使用Redis缓存的dao，基于redis缓存
 */
@Getter
@DependsOn("mainServer")
public abstract class BaseRedisCacheDao<T extends BaseIdBean> extends BaseDao<T> {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    private RedisTemplate<String, T> redisTemplate;
    private String keyprefix;
    @Autowired
    protected MainServer mainServer;
    /**
     * 二级索引，如果有，会大大降低redis的效率，慎用
     */
    public Collection<String> secondaryIndex = new HashSet<>();

    @PostConstruct
    public void init() {
        keyprefix = mainServer.conf().getSpringRedisKeyprefix() + ".com.";
        redisTemplate = new RedisTemplate<>();
        // 初始化redisTemplate
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置key和value的初始化参数
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        FastJsonRedisSerializer<T> valueSerializer = new FastJsonRedisSerializer<>(getClassT());
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
    }

    @Override
    public T insert(T object,
                    boolean override) {
        if (override) {
            redisTemplate.opsForValue().set(keyprefix + object.getId(), object);
            secondaryIndex.forEach(s -> redisTemplate.opsForValue()
                                                     .set(keyprefix + s + object.getId(), object));
        } else {
            redisTemplate.opsForValue().setIfAbsent(keyprefix + object.getId(), object);
            secondaryIndex.forEach(s -> redisTemplate.opsForValue()
                                                     .setIfAbsent(keyprefix + s + object.getId(),
                                                                  object));
        }
        return object;
    }

    @Override
    public Collection<T> insertAll(Collection<T> objects,
                                   boolean override) {
        if (objects != null) {
            for (T object : objects) {
                if (override) {
                    redisTemplate.opsForValue().set(keyprefix + object.getId(), object);
                    secondaryIndex.forEach(s -> redisTemplate.opsForValue()
                                                             .set(keyprefix + s + object.getId(),
                                                                  object));
                } else {
                    redisTemplate.opsForValue().setIfAbsent(keyprefix + object.getId(), object);
                    secondaryIndex.forEach(s -> redisTemplate.opsForValue()
                                                             .setIfAbsent(keyprefix + s
                                                                     + object.getId(), object));
                }
            }
        }
        return objects;
    }

    @Override
    public T findOne(String id) {
        return redisTemplate.opsForValue().get(keyprefix + id);
    }

    /**
     * 指定二级索引进行查找
     */
    public T findOne(String id,
                     String secondIndex) {
        if (secondaryIndex.contains(secondIndex)) {
            return redisTemplate.opsForValue().get(keyprefix + secondIndex + "." + id);
        } else {
            return null;
        }
    }

    /**
     * redis并不支持条件查询
     */
    @Override
    @Deprecated
    public T findOne(Map<String, Object> params,
                     String... fields) {
        if (params.containsKey("id")) {
            return redisTemplate.opsForValue().get(keyprefix + params.get("id"));
        } else if (params.containsKey("_id")) {
            return redisTemplate.opsForValue().get(keyprefix + params.get("_id"));
        } else {
            return null;
        }
    }

    /**
     * 这种查询并不支持params内的对象，只支持按照idlist进行查询
     */
    @Override
    @Deprecated
    public Collection<T> findAll(Collection<String> idlist,
                                 Map<String, Object> params,
                                 String... fields) {
        return redisTemplate.opsForValue().multiGet(idlist);
    }

    /**
     * 如果需要更新直接覆盖就行
     */
    @Override
    @Deprecated
    public boolean updateOne(String id,
                             Map<String, Object> kvs,
                             boolean insert) {
        return false;
    }

    /**
     * 如果需要更新直接覆盖就行
     */
    @Override
    @Deprecated
    public boolean updateAll(Collection<String> idlist,
                             Map<String, Object> kvs,
                             boolean insert) {
        return false;
    }

    @Override
    public boolean removeOne(String id) {
        secondaryIndex.forEach(s -> redisTemplate.delete(keyprefix + s + id));
        return redisTemplate.delete(keyprefix + id);
    }

    @Override
    public boolean removeAll(Collection<String> idlist) {
        secondaryIndex.forEach(s -> redisTemplate.delete(idlist.stream()
                                                               .map(i -> keyprefix + s + i)
                                                               .collect(Collectors.toList())));
        return redisTemplate.delete(idlist.stream()
                                          .map(i -> keyprefix + i)
                                          .collect(Collectors.toList())) > 0;
    }

    /**
     * 不支持条件删除
     */
    @Override
    @Deprecated
    public boolean removeAll(Map<String, Object> params) {
        return false;
    }

    /**
     * 不支持条件查询
     */
    @Override
    @Deprecated
    public List<T> query(Map<String, Object> params,
                         String sortKey) {
        return null;
    }

    /**
     * 没有好的实现方法，暂时不要
     */
    @Override
    @Deprecated
    public long count() {
        return -1;
    }

    /**
     * 没有好的实现方法，暂时不要
     */
    @Override
    @Deprecated
    public long count(Map<String, Object> params) {
        return -1;
    }

    /**
     * 这种事先不需要有mongo参与
     */
    @Override
    @Deprecated
    public BaseMongoDao<T> getMongoDao() {
        return null;
    }
}
