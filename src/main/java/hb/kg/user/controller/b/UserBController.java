package hb.kg.user.controller.b;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.dao.RoleDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.user.bean.http.HBUserAccount;
import hb.kg.user.bean.http.HBUserChangPwd;
import hb.kg.user.bean.http.HBUserStatus;
import hb.kg.user.bean.mongo.HBUser;
import hb.kg.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 用户修改，正常用户修改，只能修改自己的相关信息
 */
@Api(description = "[B]用户信息后台修改接口")
@RestController
@RequestMapping(value = "/${api.version}/b/user")
public class UserBController extends BaseCRUDController<HBUser> {
    @Autowired
    private UserService userService;
    @Resource
    public RoleDao roleDao;
    @Autowired
    private AuthService authService;

    @Override
    protected BaseCRUDService<HBUser> getService() {
        return userService;
    }

    /**
     * 用户只能更新自己的信息
     */
    @Override
    protected HBUser prepareUpdate(HBUser hbuser,
                                   ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        }
        return super.prepareUpdate(hbuser, responseBean);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUser object) {
        return super.update(object);
    }

    @Override
    protected HBUser prepareInsert(HBUser object,
                                   ResponseBean responseBean) {
        List<String> group = new ArrayList<>();
        if (object.getGroup() != null) {
            group = object.getGroup();
        }
        group.add("normal");// 所有用户归为normal用户组
        if (object.getLastPasswordResetDate() != null) {
            // 这个如果不设置，解析jwttoken会出问题
            object.setLastPasswordResetDate(new Date());
        }
        // 最后进行一下重置
        object = userService.initNewHBUser(object.toMongoHashMap());
        object.setGroup(group);
        return super.prepareInsert(object, responseBean);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBUser object) {
        return super.insert(object);
    }

    @Override
    protected HBUser postInsert(HBUser object,
                                ResponseBean responseBean) {
        return super.prepareInsert(object, responseBean);
    }

    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        return super.prepareRemove(id, responseBean);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @ApiOperation(value = "获取用户最后登录信息", notes = "获取用户最后登录信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/lastLogin", method = { RequestMethod.GET })
    public ResponseBean getLastLogin() {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isNotEmpty(userid)) {
            HBUserStatus user = mongoTemplate.findOne(Query.query(Criteria.where("id").is(userid)),
                                                      HBUserStatus.class);
            // if (StringUtils.isNotEmpty(user.getLastLoginIp())) {
            // String lastLoginLocation =
            // HttpClientUtil.httkgetRequest("http://ip.taobao.com/service/getIpInfo.php?ip="
            // + user.getLastLoginIp());
            // user.setLastLoginLocation(lastLoginLocation);
            // }
            responseBean.setData(user);
        } else {
            responseBean.setCodeAndErrMsg(ApiCode.NO_DATA.getCode(), "没有查询到有效的用户信息");
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/changPwd", method = { RequestMethod.POST })
    public ResponseBean changPassword(@RequestBody HBUserChangPwd user) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isNotEmpty(userid) && StringUtils.isNotEmpty(user.getNewPass())
                && StringUtils.isNotEmpty(user.getRePass())) {
            if (!user.getNewPass().equals(user.getRePass())) {
                responseBean.setOneData(ApiCode.NO_DATA.getCode(), "信息错误-两次输入密码不一致");
                return responseBean;
            }
            if (authService.checkPasswordValid(userid, user.getOldPass()) != null) {
                Update update = new Update();
                update.set("password", authService.encodePassword(user.getNewPass()));
                mongoTemplate.updateFirst(new Query(Criteria.where("id").is(userid)),
                                          update,
                                          HBUserAccount.class);
            } else {
                responseBean.setOneData(ApiCode.NO_DATA.getCode(), "密码错误");
            }
        } else {
            responseBean.setOneData(ApiCode.NO_DATA.getCode(), "没有查询到有效的用户信息");
        }
        return responseBean;
    }
}
