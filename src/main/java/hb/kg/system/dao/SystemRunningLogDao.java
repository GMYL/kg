package hb.kg.system.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.system.bean.mongo.HBSystemRunningLog;

@Repository("systemRunningLogDao")
public class SystemRunningLogDao extends BaseMongoDao<HBSystemRunningLog> {
}
