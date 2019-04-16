package hb.kg.common.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.bean.auth.HBRole;

@Repository("roleDao")
public class RoleDao extends BaseLocalMongoCacheDao<HBRole> {
}
