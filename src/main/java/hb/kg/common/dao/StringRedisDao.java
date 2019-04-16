package hb.kg.common.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import hb.kg.common.server.MainServer;
import hb.kg.common.util.set.HBCollectionUtil;

/**
 * 通用的redis保存数据的对象，这里的对象都是Object，用户用的时候需要自己进行类型转换
 */
@DependsOn("sysConfService")
@Repository("stringRedisDao")
public class StringRedisDao {
    @Autowired
    private MainServer mainServer;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    private static RedisTemplate<String, String> redisTemplate;
    private static String keyprefix;

    @PostConstruct
    public void init() {
        // 初始化redis的keyprefix，组成是系统前缀.collection名称.
        keyprefix = mainServer.conf().getSpringRedisKeyprefix() + ".general.";
        redisTemplate = new RedisTemplate<>();
        // 初始化redisTemplate
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置key和value的初始化参数
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.afterPropertiesSet();
    }

    public String findOne(String key) {
        return redisTemplate.opsForValue().get(keyprefix + key);
    }

    public void insertOne(String key,
                          String value) {
        redisTemplate.opsForValue().set(keyprefix + key, value);
    }

    public void insertOne(String key,
                          String value,
                          long timeout) {
        redisTemplate.opsForValue().set(keyprefix + key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 更新用户的在线情况
     */
    public void updateAdminOnline(String userid) {
        redisTemplate.opsForValue()
                     .set(keyprefix + "admin." + userid, "true", 300, TimeUnit.SECONDS);
    }

    /**
     * 获取所有在线的用户
     */
    public List<String> getUserOnline() {
        String adminPrefix = keyprefix + "admin.";
        Set<String> keys = redisTemplate.keys(keyprefix + "admin.");
        if (HBCollectionUtil.isNotEmpty(keys)) {
            return keys.stream()
                       .map(k -> k.substring(adminPrefix.length()))
                       .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
}
