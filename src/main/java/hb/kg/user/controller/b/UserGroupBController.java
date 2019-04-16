package hb.kg.user.controller.b;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.auth.HBRole;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.service.RoleService;
import hb.kg.user.bean.http.HBUserBasic;
import hb.kg.user.bean.http.HBUserViceLeader;
import hb.kg.user.bean.mongo.HBUserGroup;
import hb.kg.user.service.UserGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 用户组管理
 */
@Api(description = "[B]用户组管理接口")
@RestController
@RequestMapping(value = "/${api.version}/b/usergroup")
public class UserGroupBController extends BaseCRUDController<HBUserGroup> {
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private RoleService roleService;

    @Override
    protected BaseCRUDService<HBUserGroup> getService() {
        return userGroupService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBUserGroup object) {
        return super.query(object);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUserGroup object) {
        return super.update(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBUserGroup object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @Override
    protected HBUserGroup prepareUpdate(HBUserGroup object,
                                        ResponseBean responseBean) {
        if (object.getViceLeaders() != null) {// 为用户组管理副组长显示做下处理
            List<HBUserBasic> userList = new ArrayList<>();
            for (HBUserBasic hbUserBasic : object.getViceLeaders()) {
                HBUserViceLeader ViceLeader = new HBUserViceLeader();
                ViceLeader = mongoTemplate.findOne(new Query(Criteria.where("id")
                                                                     .is(hbUserBasic.getId())
                                                                     .and("group")
                                                                     .all(object.getId())),
                                                   HBUserViceLeader.class);
                if (ViceLeader != null) {
                    hbUserBasic = HBUserViceLeader.toHBUserBasic(ViceLeader);
                    userList.add(hbUserBasic);
                }
            }
            object.setViceLeaders(userList);
        }
        if (object.getRoles() != null) { // 增加roles中文介绍
            List<String> roles = new ArrayList<String>();
            List<String> rnames = new ArrayList<String>();
            for (String role : object.getRoles()) {
                HBRole r = roleService.dao().findOne(role);
                if (r == null) {
                    roles.add(role);
                } else {
                    rnames.add(r.getName());
                }
            }
            object.getRoles().removeAll(roles);
            object.setRolesname(rnames);
        }
        return super.prepareUpdate(object, responseBean);
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @ApiOperation(value = "获取后台初始化系统需要用到的信息", notes = "获取后台初始化系统需要用到的信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/init", method = { RequestMethod.GET })
    public ResponseBean getInitSysTags() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(userGroupService.dao().findAll());
        return returnBean(responseBean);
    }
}
