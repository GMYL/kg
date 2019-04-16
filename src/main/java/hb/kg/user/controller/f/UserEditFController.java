package hb.kg.user.controller.f;

import org.apache.commons.lang3.StringUtils;
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
import hb.kg.user.bean.http.HBUserBasic;
import hb.kg.user.bean.http.HBUserSelfEdit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台对各个用户数据的管理
 */
@Api(description = "[F]用户数据管理接口")
@RestController
@RequestMapping(value = "/${api.version}/f/userself")
public class UserEditFController extends BaseBeanCRUDController<HBUserSelfEdit> {
    @Override
    protected String prepareGet(String id,
                                ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isNoneEmpty(userid) && StringUtils.isNotEmpty(id) && userid.equals(id)) {
        } else {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户只能访问自己的数据");
        }
        return super.prepareGet(id, responseBean);
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @ApiOperation(value = "得到用户基本信息", notes = "得到用户基本信息", produces = "application/json")
    @RequestMapping(value = "/basic", method = { RequestMethod.GET })
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

    @Override
    protected HBUserSelfEdit prepareUpdate(HBUserSelfEdit user,
                                           ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isNoneEmpty(userid) && StringUtils.isNotEmpty(user.getId())
                && userid.equals(user.getId())) {
            // 阻塞用户修改自己的用户名或手机号或邮箱（这三个不从这里修改）
            user.setUserName(null);
            user.setPhoneNum(null);
            user.setEmail(null);
        } else {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户只能修改自己的账户");
        }
        return super.prepareUpdate(user, responseBean);
    }
    
    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUserSelfEdit object) {
        return super.update(object);
    }
    
}
