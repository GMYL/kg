package hb.kg.system.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseLocalMongoCacheDao;
import hb.kg.system.bean.mongo.HBModule;

@Repository("moduleDao")
public class ModuleDao extends BaseLocalMongoCacheDao<HBModule> {
}
