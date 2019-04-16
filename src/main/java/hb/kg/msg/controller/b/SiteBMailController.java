package hb.kg.msg.controller.b;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.msg.bean.http.HBMultiSiteMail;
import hb.kg.msg.bean.http.HBSiteMailSome;
import hb.kg.msg.bean.mongo.HBSiteMail;
import hb.kg.msg.bean.mongo.SiteMailFactory;
import hb.kg.msg.service.SiteMailService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "[B]站内信管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/msg/sitemail" })
public class SiteBMailController extends BaseCRUDController<HBSiteMail> {
    @Autowired
    private SiteMailService siteMailService;

    @Override
    protected BaseCRUDService<HBSiteMail> getService() {
        return siteMailService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBSiteMail object) {
        return super.query(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBSiteMail object) {
        return super.update(object);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @Resource
    private MongoTemplate mongoTemplate;

    @ApiOperation(value = "发送站内信", notes = "发送站内信", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "send", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean sendSiteMail(@ApiParam(value = "信息内容", required = true) @RequestBody HBSiteMail mail) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            return returnBean(responseBean);
        } else {
            mail.setFrom(new HBUserBasic(userid));
            if (SiteMailFactory.checkSitemailValid(mail)) {
                siteMailService.sendInternMail(mail);
            } else {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("站内信缺失信息");
            }
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/sendAll", method = { RequestMethod.POST })
    public ResponseBean sendSiteMailAll(@ApiParam(value = "信息内容", required = true) @RequestBody HBSiteMailSome mails) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            return returnBean(responseBean);
        } else {
            for (String toid : mails.getIdList()) {
                HBSiteMail mail = new HBSiteMail();
                mail.prepareHBBean();
                mail.setTo(new HBUserBasic(toid));
                mail.setFrom(new HBUserBasic(userid));
                mail.setContent(mails.getContent());
                mail.setTitle(mails.getTitle());
                if (SiteMailFactory.checkSitemailValid(mail)) {
                    siteMailService.sendInternMail(mail);
                } else {
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("站内信缺失信息");
                }
            }
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "发送公告", notes = "发送公告，发送系统公告", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "sendmul", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean sendMulSiteMail(@ApiParam(value = "信息内容") @RequestBody HBMultiSiteMail mails) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        }
        mails.setFromId(userid);
        if (SiteMailFactory.checkMultiSitemailValid(mails)) {
            siteMailService.sendMultiInternMail(mails);
        } else {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("站内信缺失信息");
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "查看站内信", notes = "查看用户的站内信", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/view/{id}", method = { RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean viewSiteMail(@ApiParam(value = "用户ID", required = true) @PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(id)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("id").is(id),
                                                         Criteria.where("to").is(userid)));
            Update update = new Update();
            update.set("isRead", true);
            mongoTemplate.updateFirst(query, update, HBSiteMail.class);
        }
        return returnBean(responseBean);
    }

    @Override
    protected HBSiteMail prepareInsert(HBSiteMail object,
                                       ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid) || (!userid.equals(object.getFrom().getId()))) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        }
        return super.prepareInsert(object, responseBean);
    }

    @Override
    protected HBSiteMail prepareUpdate(HBSiteMail object,
                                       ResponseBean responseBean) {
        return super.prepareUpdate(object, responseBean);
    }

    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        return super.prepareRemove(id, responseBean);
    }

    @Override
    protected HBSiteMail prepareQuery(HBSiteMail object,
                                      ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        // 收件人或发件人要有一个是自己
        if (StringUtils.isEmpty(userid)
                || (!(userid.equals(object.getTo() != null ? object.getTo().getId() : null)
                        || userid.equals(object.getFrom() != null ? object.getFrom().getId()
                                : null)))) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        }
        return super.prepareQuery(object, responseBean);
    }

    // 假删除
    @ApiOperation(value = "假删除", notes = "信息-假删除", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/fakedel", method = { RequestMethod.POST })
    public ResponseBean fakedel(@ApiParam(value = "查询条件") @RequestBody HBSiteMail mail) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else if (StringUtils.isNotEmpty(mail.getId())) {
            Map<String, Object> params = new HashMap<>();
            if (mail.getTo() != null && userid.equals(mail.getTo().getId())) {
                // 接收人删除
                params.put("toValid", false);
            }
            if (mail.getFrom() != null && userid.equals(mail.getFrom().getId())) {
                params.put("fromValid", false);
                // 发送人删除
            }
            params.put("id", mail.getId());
            siteMailService.dao().updateOne(mail.getId(), params);
        }
        return returnBean(responseBean);
    }

    /**
     * 复杂查询，没必要写到dao中，因为肯定只有这里会用
     */
    @ApiOperation(value = "得到消息-复杂查询", notes = "得到消息-复杂查询，包含收件箱、发件箱、已删除、所有的", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/load", method = { RequestMethod.POST })
    public ResponseBean getContentById(@ApiParam(value = "查询条件") @RequestBody HBSiteMail mail) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else if (mail.getTo() != null && userid.equals(mail.getTo().getId())) {
            // 收件箱
            query.addCriteria(new Criteria().andOperator(Criteria.where("to").is(userid),
                                                         Criteria.where("toValid").is(true)));
        } else if (mail.getFrom() != null && userid.equals(mail.getFrom().getId())) {
            // 发件箱
            query.addCriteria(new Criteria().andOperator(Criteria.where("from").is(userid),
                                                         Criteria.where("fromValid").is(true)));
        } else if (mail.getToValid() != null && (!mail.getToValid()) && mail.getFromValid() != null
                && (!mail.getFromValid())) {
            // 已删除
            query.addCriteria(new Criteria().orOperator(new Criteria().andOperator(Criteria.where("to")
                                                                                           .is(userid),
                                                                                   Criteria.where("toValid")
                                                                                           .is(false)),
                                                        new Criteria().andOperator(Criteria.where("from")
                                                                                           .is(userid),
                                                                                   Criteria.where("fromValid")
                                                                                           .is(false))));
        } else {
            // 所有的
            query.addCriteria(new Criteria().orOperator(new Criteria().andOperator(Criteria.where("to")
                                                                                           .is(userid),
                                                                                   Criteria.where("toValid")
                                                                                           .is(true)),
                                                        new Criteria().andOperator(Criteria.where("from")
                                                                                           .is(userid),
                                                                                   Criteria.where("fromValid")
                                                                                           .is(true))));
        }
        if (mail.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "createTime"));
            Sort sort = Sort.by(orders);
            mail.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBSiteMail.class);
            mail.getPage().setTotalSize(count.intValue());
            query.with(mail.getPage());
        }
        List<HBSiteMail> list = mongoTemplate.find(query, HBSiteMail.class);
        mail.getPage().setList(list);
        responseBean.setData(mail.getPage());
        return returnBean(responseBean);
    }
}
