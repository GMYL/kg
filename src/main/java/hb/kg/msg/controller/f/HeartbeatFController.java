package hb.kg.msg.controller.f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.HeartbeatBean;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseController;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.util.json.JSONObject;
import hb.kg.msg.service.SiteMailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 这个类用于处理heartbeat的信号
 */
@Api(description = "[F]用于处理heartbeat的信号")
@RestController
@RequestMapping(value = "/${api.version}/f/msg/heartbeat")
public class HeartbeatFController extends BaseController {
    @Autowired
    private SiteMailService siteMailService;
    @Resource
    private StringRedisDao stringRedisDao;

    @ApiOperation(value = "信息监测", notes = "信息监测，检查用户是否需要访问数据库，检查用户是否有站内信", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/check", method = { RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean check(HeartbeatBean heartbeat) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (!StringUtils.isEmpty(userid)) {
            Map<String, Object> rsMap = new HashMap<>();
            responseBean.setData(rsMap);
            // 检查用户是否有站内信
            Map<String, String> msgs = siteMailService.checkMongoInternalMail(userid);
            if (msgs != null) {
                List<JSONObject> msgList = new ArrayList<>();
                Iterator<Entry<String, String>> itr = msgs.entrySet().iterator();
                while (itr.hasNext()) {
                    Entry<String, String> entry = itr.next();
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("id", entry.getKey());
                    jsobj.put("content", entry.getValue());
                    msgList.add(jsobj);
                }
                rsMap.put("msgs", msgList);
            }
            // 更新redis中用户的状态
            stringRedisDao.updateAdminOnline(userid);
        }
        return returnBean(responseBean);
    }
}
