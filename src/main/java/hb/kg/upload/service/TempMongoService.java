package hb.kg.upload.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.bean.mongo.Page;
import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.upload.bean.mongo.HBTempMongoBean;
import hb.kg.upload.dao.TempMongoDao;
import hb.kg.user.bean.http.HBUserBasic;

@Service
public class TempMongoService extends BaseCRUDService<HBTempMongoBean> {
    @Resource
    private TempMongoDao tempMongoDao;

    @Override
    public BaseDao<HBTempMongoBean> dao() {
        return tempMongoDao;
    }

    /**
     * 这里阻断一下用户的上传，因为用户的id需要是string
     */
    public Page<HBTempMongoBean> queryToFields(Map<String, Object> params,
                                               Page<HBTempMongoBean> page,
                                               List<String> filedsNameList) {
        HBUserBasic user = (HBUserBasic) params.get("user");
        if (user != null) {
            params.put("user", user.getId());
        }
        return dao().getMongoDao().queryToFields(params, page, filedsNameList);
    }

    public List<HBTempMongoBean> findManyByIdAndRemove(List<String> idList) {
        return tempMongoDao.findManyByIdAndRemove(idList);
    }

    public void removeExistings(String userid,
                                List<String> idList) {
        tempMongoDao.removeExistings(userid, idList);
    }
}
