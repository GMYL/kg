package hb.kg.user.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.user.bean.http.HBUserUpload;
import hb.kg.user.dao.UserUploadDao;

@Service
public class UserUploadService extends BaseCRUDService<HBUserUpload> {
    @Resource
    private UserUploadDao userUploadDao;

    @Override
    public BaseDao<HBUserUpload> dao() {
        return userUploadDao;
    }
}
