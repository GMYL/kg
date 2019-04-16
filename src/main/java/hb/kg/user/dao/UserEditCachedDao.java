package hb.kg.user.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.user.bean.http.HBUserEdit;

@Repository("userEditCachedDao")
public class UserEditCachedDao extends BaseMongoDao<HBUserEdit> {
}
