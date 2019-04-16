package hb.kg.system.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.system.bean.mongo.HBModuleCategory;
import hb.kg.system.dao.ModuleCategoryDao;

@Service
public class ModuleCategoryService extends BaseCRUDService<HBModuleCategory> {
    @Resource
    private ModuleCategoryDao moduleCategoryDao;

    @Override
    public BaseDao<HBModuleCategory> dao() {
        return moduleCategoryDao;
    }
}
