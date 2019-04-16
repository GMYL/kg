package hb.kg.common.dao;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.util.json.spring.FastJsonRedisSerializer;
import hb.kg.common.util.set.HBCollectionUtil;
import lombok.Getter;

/**
 * 对Mongo使用Redis缓存的dao，基于redis缓存
 * @WARN redis没法在数据过期的时候自动刷写数据库，因此不能用在需要统计点击量之类的需要将缓存更新到数据库的场合
 * @INFO redis缓存服务适合保存时间较长、且数据大小较大的数据
 * @WARN 还没有添加过期回收的相关功能
 */
@Getter
public class BaseRedisMongoCacheDao<T extends BaseMgBean<T>> extends BaseDao<T> {
    @Resource
    protected MongoTemplate mongoTemplate;
    private BaseMongoDao<T> baseMongoDao;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    private RedisTemplate<String, T> redisTemplate;
    private String keyprefix;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        // 设置
        Class<T> myClassT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Document doc = myClassT.getAnnotation(Document.class);
        baseMongoDao = new BaseMongoDao<T>(mainServer, mongoTemplate, myClassT) {};
        if (doc != null) {
            // 初始化redis的keyprefix，组成是系统前缀.collection名称.
            keyprefix = mainServer.conf().getSpringRedisKeyprefix() + "." + myClassT.getSimpleName()
                    + ".";
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
        } else {
            logger.error("FATAL！加载的数据没有Document注解");
            System.exit(-1);
        }
    }

    /**
     * 缓存的插入操作需要首先清空缓存内当前存在id的对象，然后全部直接插入到数据库中
     */
    @Override
    public T insert(T object,
                    boolean override) {
        redisTemplate.delete(keyprefix + object.getId());
        return baseMongoDao.insert(object, override);
    }

    /**
     * 缓存的插入操作需要首先清空缓存内当前存在id的对象，然后全部直接插入到数据库中
     */
    @Override
    public Collection<T> insertAll(Collection<T> objects,
                                   boolean override) {
        if (HBCollectionUtil.isNotEmpty(objects)) {
            redisTemplate.delete(keyprefix
                    + objects.stream().map(o -> o.getId()).collect(Collectors.toList()));
            return baseMongoDao.insertAll(objects, override);
        }
        return null;
    }

    /**
     * 按照id号查找，先查询缓存，没有命中才去数据库内查询
     */
    public T findOne(String id) {
        T obj = redisTemplate.opsForValue().get(keyprefix + id);
        if (obj != null) {
            return obj;
        } else {
            obj = baseMongoDao.findOne(id);
            redisTemplate.opsForValue()
                         .set(keyprefix + id, obj, obj.getTimeout(), TimeUnit.SECONDS);
        }
        return obj;
    }

    /**
     * 按照查询条件的查找，直接走数据库，然后填入缓存
     */
    @Override
    public T findOne(Map<String, Object> params,
                     String... fields) {
        T obj = baseMongoDao.findOne(params, fields);
        if (obj != null) {
            redisTemplate.opsForValue()
                         .set(keyprefix + obj.getId(), obj, obj.getTimeout(), TimeUnit.SECONDS);
        }
        return obj;
    }

    /**
     * 这类列表访问就直接走数据库就行，不需要添加缓存，且也不需要更新访问量
     */
    @Override
    public Collection<T> findAll(Collection<String> idlist,
                                 Map<String, Object> params,
                                 String... fields) {
        return baseMongoDao.findAll(idlist, params, fields);
    }

    @Override
    public boolean updateOne(String id,
                             Map<String, Object> kvs,
                             boolean insert) {
        redisTemplate.delete(keyprefix + id);
        return baseMongoDao.updateOne(id, kvs, insert);
    }

    @Override
    public boolean updateAll(Collection<String> idlist,
                             Map<String, Object> kvs,
                             boolean insert) {
        redisTemplate.delete(HBCollectionUtil.addPreAndPostFix(idlist, keyprefix, ""));
        return baseMongoDao.updateAll(idlist, kvs, insert);
    }

    /**
     * 删除数据的话，删除缓存中的数据并从数据库清空
     */
    @Override
    public boolean removeOne(String id) {
        redisTemplate.delete(keyprefix + id);
        return baseMongoDao.removeOne(id);
    }

    /**
     * 删除数据的话，删除缓存中的数据并从数据库清空
     */
    @Override
    public boolean removeAll(Collection<String> idlist) {
        redisTemplate.delete(HBCollectionUtil.addPreAndPostFix(idlist, keyprefix, ""));
        return baseMongoDao.removeAll(idlist);
    }

    /**
     * 如果是按条件删除，需要先把这些待删除的数据都先查询出来
     * @WARN 这样做的效率会很低，而且一不小心服务器就挂了，所以务必务必尽量不要这样做
     */
    @Override
    public boolean removeAll(Map<String, Object> params) {
        Collection<T> objects = baseMongoDao.findAll(params, "_id");
        if (HBCollectionUtil.isNotEmpty(objects)) {
            redisTemplate.delete(objects.stream()
                                        .map(o -> keyprefix + o.getId())
                                        .collect(Collectors.toList()));
        }
        return baseMongoDao.removeAll(params);
    }

    @Override
    public List<T> query(Map<String, Object> params,
                         String sortKey) {
        return baseMongoDao.query(params, sortKey);
    }

    @Override
    public Class<T> getClassT() {
        return baseMongoDao.getClassT();
    }

    @Override
    public long count() {
        return baseMongoDao.count();
    }

    @Override
    public long count(Map<String, Object> params) {
        return baseMongoDao.count(params);
    }

    @Override
    public BaseMongoDao<T> getMongoDao() {
        return baseMongoDao;
    }
}
