package hb.kg.common.controller;

import java.lang.reflect.ParameterizedType;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.dao.BaseDao;
import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.service.FullBaseCRUDService;

/**
 * 对于一些对象，可以直接封装获取的方式，不需要复杂化，从UserEditFController的方式演化而来
 */
@RestController
public abstract class BaseBeanCRUDController<T extends BaseMgBean<T>>
        extends BaseCRUDController<T> {
    protected BaseCRUDService<T> baseService;
    protected BaseDao<T> baseDao;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void fullAutoInit() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        baseDao = new BaseMongoDao<T>(mainServer,
                                      mongoTemplate,
                                      (Class<T>) pt.getActualTypeArguments()[0]) {};
        FullBaseCRUDService<T> service = new FullBaseCRUDService<T>(mongoTemplate,
                                                                    mainServer,
                                                                    timerService,
                                                                    baseDao) {};
        baseService = service;
    }

    @Override
    protected BaseCRUDService<T> getService() {
        return baseService;
    }
}
