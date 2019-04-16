package hb.kg.system.controller.b;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseController;
import hb.kg.system.service.SystemBackupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 系统备份接口
 */
@Api(description = "[B]系统备份接口")
@RestController
@RequestMapping(value = { "/${api.version}/b/system/backup" })
public class SystemBackupController extends BaseController {
    @Autowired
    private SystemBackupService systemBackupService;

    /**
     * 查看当前系统的所有的备份文件
     */
    @ApiOperation(value = "查看当前系统的所有的备份文件", notes = "查看当前系统的所有的备份文件", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/list", method = { RequestMethod.GET })
    public ResponseBean getBackupList() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(systemBackupService.listAllFile());
        return returnBean(responseBean);
    }

    /**
     * 手动触发备份
     */
    @ApiOperation(value = "手动触发备份", notes = "手动触发备份", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/man/generate", method = { RequestMethod.GET })
    public ResponseBean generateBackup() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(systemBackupService.doBackUpMongoDB());
        return returnBean(responseBean);
    }

    /**
     * 还原数据库
     */
    @ApiOperation(value = "还原数据库", notes = "还原数据库", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/reduction/{id}", method = { RequestMethod.GET })
    public ResponseBean reductionDatabase(@ApiParam("备份文件id") @PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(id)) {
            responseBean.setData(systemBackupService.reductionMongoDB(id));
        } else {
            responseBean.setCodeAndErrMsg(ApiCode.NO_DATA.getCode(), "没有传入文件名");
        }
        return returnBean(responseBean);
    }

    /**
     * 删除备份文件
     */
    @ApiOperation(value = "删除备份文件", notes = "删除备份文件", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean deleteDatabase(@ApiParam("备份文件id") @PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(id)) {
            responseBean.setData(systemBackupService.removeBackupFile(id));
        } else {
            responseBean.setCodeAndErrMsg(ApiCode.NO_DATA.getCode(), "没有传入文件名");
        }
        return returnBean(responseBean);
    }
}
