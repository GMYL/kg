package hb.kg.common.controller.auth.b;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.auth.HBRole;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.service.RoleService;
import hb.kg.system.bean.mongo.HBModule;
import hb.kg.system.service.ModuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 角色管理
 */
@Api(description = "[B]角色管理")
@RestController
@RequestMapping(value = "/${api.version}/b/role")
public class RoleBController extends BaseCRUDController<HBRole> {
    @Autowired
    private RoleService roleService;
    @Autowired
    private ModuleService moduleService;

    @Override
    protected BaseCRUDService<HBRole> getService() {
        return roleService;
    }

    /**
     * 增加Modulesname字段
     */
    @Override
    protected HBRole prepareUpdate(HBRole role,
                                   ResponseBean responseBean) {
        if (role.getModules() != null) {
            List<String> mnames = new ArrayList<String>();
            for (String modules : role.getModules()) {
                HBModule module = moduleService.dao().findOne(modules);
                mnames.add(module.getName());
            }
            role.setModulesname(mnames);
        }
        return super.prepareUpdate(role, responseBean);
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @ApiOperation(value = "得到初始化系统标签(角色)", notes = "得到初始化系统标签(角色)，获取后台初始化系统需要用到的信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/init", method = { RequestMethod.GET })
    public ResponseBean getInitSysTags() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(roleService.dao().findAll());
        return returnBean(responseBean);
    }
}
