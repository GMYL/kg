package hb.kg.user.controller.b;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseBeanCRUDController;
import hb.kg.common.service.AuthService;
import hb.kg.common.util.id.HeadUtil;
import hb.kg.user.bean.http.HBUserAccount;
import hb.kg.user.bean.http.HBUserBasic;
import hb.kg.user.bean.http.HBUserEdit;
import hb.kg.user.bean.mongo.HBUser;
import hb.kg.user.bean.mongo.HBUserVipState;
import hb.kg.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台对各个用户数据的管理
 */
@Api(description = "[B]用户管理接口")
@RestController
@RequestMapping(value = "/${api.version}/b/user/edit")
public class UserEditBController extends BaseBeanCRUDController<HBUserEdit> {
    
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    
    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @Override
    protected HBUserEdit prepareUpdate(HBUserEdit user,
                                       ResponseBean responseBean) {
        HBUserAccount userAccount = mongoTemplate.findOne(new Query(Criteria.where("id")
                                                                            .is(user.getId())),
                                                          HBUserAccount.class);
        if (userAccount != null && !userAccount.getPassword().equals(user.getPassword())) {
            user.setPassword(authService.encodePassword(user.getPassword()));
        }
        return super.prepareUpdate(user, responseBean);
    }
    
    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUserEdit object) {
        return super.update(object);
    }

    @ApiOperation(value = "获取用户信息", notes = "获取用户信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/basic/{id}", method = { RequestMethod.GET })
    public ResponseBean getUserBasic(@ApiParam(value = "用户ID", required = true) @PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isEmpty(id)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("id号未传入");
        } else {
            responseBean.setData(mongoTemplate.findOne(new Query(Criteria.where("id").is(id)),
                                                       HBUserBasic.class,
                                                       "hb_users"));
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "得到所有管理员信息", notes = "得到所有管理员信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/alladmins", method = { RequestMethod.GET })
    public ResponseBean getAllAdmins() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(userService.getAllAdmin());
        return returnBean(responseBean);
    }

    @ApiOperation(value = "增加一个房地产vip用户", notes = "增加一个房地产vip用户", produces = "application/json")
    @RequestMapping(value = "/addUserV5", method = { RequestMethod.PUT })
    public ResponseBean addUserV5(@RequestBody HBUser object) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotEmpty(object.getUserName())
                && StringUtils.isNotEmpty(object.getPassword())
                && StringUtils.isNotEmpty(object.getVipState().getLevel())
                && object.getVipState().getExpireDays() != null) {
            object.prepareHBBean();
            List<String> list = object.getGroup();
            list.add("normal");
            object.setGroup(list);
            object.setPassword(authService.encodePassword(object.getPassword()));
            object.setLastPasswordResetDate(new Date());
            HBUserVipState vipSteate = object.getVipState();
            vipSteate.setStartDay(new Date());
            object.setLogo(HeadUtil.initHead(object.getGender()));
            object.setVip(5);
            object.setVipState(vipSteate);
            // object.setState(HBUserStateEnum.USER_ALREADY_ACTIVATE.getIndex());
            userService.dao().insert(object);
        } else {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("信息不全");
        }
        responseBean.setData(userService.getAllAdmin());
        return returnBean(responseBean);
    }

    /**
     * 注册用户管理查询，只查询注册用户组，
     */
    @RequestMapping(value = "/normalquery", method = { RequestMethod.POST })
    public ResponseBean getContentById(@RequestBody HBUserEdit searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        if (searhBean.getId() != null) {
            query.addCriteria(Criteria.where("id").is(searhBean.getId()));
        }
        if (searhBean.getValid() != null) {
            query.addCriteria(Criteria.where("valid").is(searhBean.getValid()));
        }
        if (searhBean.getUserName() != null) {
            query.addCriteria(Criteria.where("userName").is(searhBean.getUserName()));
        }
        if (searhBean.getTrueName() != null) {
            query.addCriteria(Criteria.where("trueName").is(searhBean.getTrueName()));
        }
        if (searhBean.getCompany() != null) {
            query.addCriteria(Criteria.where("company").is(searhBean.getCompany()));
        }
        query.addCriteria(Criteria.where("group").nin(searhBean.getGroup()));
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "regDate"));
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBUserEdit.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBUserEdit> list = mongoTemplate.find(query, HBUserEdit.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }
}
