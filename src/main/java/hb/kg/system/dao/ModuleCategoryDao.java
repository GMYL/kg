package hb.kg.system.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseTreeLocalMongoCacheDao;
import hb.kg.system.bean.mongo.HBModuleCategory;

@Repository("moduleCategoryDao")
public class ModuleCategoryDao extends BaseTreeLocalMongoCacheDao<HBModuleCategory> {
}
