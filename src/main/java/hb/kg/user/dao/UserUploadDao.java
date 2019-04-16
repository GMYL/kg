package hb.kg.user.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.user.bean.http.HBUserUpload;

@Repository("userUploadDao")
public class UserUploadDao extends BaseMongoDao<HBUserUpload> {
}
