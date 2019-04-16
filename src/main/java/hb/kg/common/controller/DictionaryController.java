package hb.kg.common.controller;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.anno.RegisterMethod;
import hb.kg.common.bean.enums.ReturnCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.bean.job.ControllerJob;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.service.RoleService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.common.util.time.TimeUtil;
import hb.kg.system.service.ModuleService;
import hb.kg.user.service.UserGroupService;
import hb.kg.user.service.UserService;

/**
 * 返回数据常用字典数据
 */
@RestController
@RequestMapping(value = { "/${api.version}/b/dictionary", "/${api.version}/f/dictionary" })
public class DictionaryController extends BaseController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private UserService userService;
    @Resource
    private StringRedisDao stringRedisDao;

    @PostConstruct
    private void initget() {
        try {
            registerJob(new ControllerJob("initb",
                                          DictionaryController.class.getMethod("getDicB"),
                                          this));
            registerJob(new ControllerJob("initf",
                                          DictionaryController.class.getMethod("getDicF"),
                                          this));
        } catch (Exception e) {
            logger.error("注册方法失败", e);
            mainServer.shutdown(ReturnCode.INTERNAL_ERROR.getCode());
        }
    }

    @Override
    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RegisterMethod()
    public JSONObject getDicB() {
        JSONObject rsMap = new JSONObject();
        rsMap.put("b", "b");
        return rsMap;
    }

    @RegisterMethod(name = "getDicF")
    public JSONObject getDicF() {
        JSONObject rsMap = new JSONObject();
        rsMap.put("b", "b");
        return rsMap;
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @RegisterMethod()
    public ResponseBean init() {
        ResponseBean responseBean = getReturn();
        String key = TimeUtil.getStringFromFreq(new Date(), "hour") + ".dic.b";
        String rsMapStr = stringRedisDao.findOne(key);
        JSONObject rsMap = null;
        if (HBStringUtil.isNotBlank(rsMapStr)) {
            rsMap = JSONObject.parseObject(rsMapStr);
        } else {
            rsMap = new JSONObject();
            rsMap.put("admins", userService.getAllAdmin());
            rsMap.put("roles", roleService.dao().findAll());
            rsMap.put("modules", moduleService.dao().findAll());
            rsMap.put("groups", userGroupService.dao().findAll());
            rsMap.put("downloadPrefix", mainServer.conf().getDownloadUrlPrefix());
            rsMap.put("sessionId", userService.getSessionCode());
            stringRedisDao.insertOne(key, rsMap.toJSONString(), 5 * 60); // 每5min强制数据过期
        }
        responseBean.setData(rsMap);
        return returnBean(responseBean);
    }
}
