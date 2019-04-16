package hb.kg.user.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseLocalMongoCacheDao;
import hb.kg.user.bean.mongo.HBUserGroup;

@Repository("userGroupDao")
public class UserGroupDao extends BaseLocalMongoCacheDao<HBUserGroup> {
}
