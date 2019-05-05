package hb.kg.law.service;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.law.bean.mongo.HBLawitem;
import hb.kg.law.dao.LawitemDao;

@Service
public class LawItemService extends BaseCRUDService<HBLawitem> {
    @Resource
    private LawitemDao lawitemDao;

    public BaseMongoDao<HBLawitem> dao() {
        return lawitemDao;
    }

    public HBLawitem insert(HBLawitem object) {
        // 写mongo
        object = dao().insert(object);
        return object;
    }

    public HashMap<String, String> loadmany(List<String> ids) {
        return lawitemDao.getMany(ids);
    }

    /**
     * 解决其它模块数据调用，这里可以加入截断，对用户的调用数据进行截断并本类的特有功能
     */
    public Object solveData(JSONObject data) {
        return super.solveData(data);
    }
}
