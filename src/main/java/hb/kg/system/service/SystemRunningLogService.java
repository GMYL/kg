package hb.kg.system.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.system.bean.mongo.HBSystemRunningLog;
import hb.kg.system.dao.SystemRunningLogDao;

@Service
public class SystemRunningLogService extends BaseCRUDService<HBSystemRunningLog> {
    @Resource
    private SystemRunningLogDao systemRunningLogDao;

    public BaseMongoDao<HBSystemRunningLog> dao() {
        return systemRunningLogDao;
    }

    public void addSystemRunningLog(Class<?> clazz,
                                    String content) {
        addSystemRunningLog(clazz.getName(), content);
    }

    public void addSystemRunningLog(String type,
                                    String content) {
        HBSystemRunningLog srl = new HBSystemRunningLog();
        srl.setType(type);
        srl.setContent(content);
        srl.prepareHBBean();
        dao().insert(srl);
    }
}
