package hb.kg.system.controller.b;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.system.bean.mongo.HBSystemRunningLog;
import hb.kg.system.service.SystemRunningLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "[B]系统运行日志")
@RestController
@RequestMapping(value = { "/${api.version}/b/system/running/log" })
public class SystemRunningLogBController extends BaseCRUDController<HBSystemRunningLog> {
    @Autowired
    private SystemRunningLogService systemRunningLogService;

    @Override
    protected BaseCRUDService<HBSystemRunningLog> getService() {
        return systemRunningLogService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBSystemRunningLog object) {
        return super.query(object);
    }

    /***
     * 按照period访问数据
     */
    @ApiOperation(value = "通过period查询数据", notes = "按照period访问数据", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/period", method = { RequestMethod.POST })
    public ResponseBean queryByPeroid(@ApiParam("查询条件") @RequestBody HBSystemRunningLog periodBean) {
        ResponseBean responseBean = getReturn();
        // 必须有查询的起始时间，如果没有终止时间，默认是第二天
        if (periodBean.getTimeStart() != null) {
            Date endtime = periodBean.getTimeEnd() != null ? periodBean.getTimeEnd()
                    : new Date(System.currentTimeMillis() + 24l * 3600 * 1000);
            Query query = new Query(Criteria.where("createTime")
                                            .gte(periodBean.getTimeStart())
                                            .lt(endtime));
            List<HBSystemRunningLog> basicStatus = mongoTemplate.find(query,
                                                                      HBSystemRunningLog.class);
            responseBean.setData(basicStatus);
        } else {
            responseBean.setCodeAndErrMsg(ApiCode.PARAM_CONTENT_ERROR.getCode(), "必须设定查询的起始时间");
        }
        return returnBean(responseBean);
    }
}
