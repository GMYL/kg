package hb.kg.system.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.system.bean.mongo.HBSystemInfo;

@Repository("systemInfoDao")
public class SystemInfoDao extends BaseMongoDao<HBSystemInfo> {
}
