package hb.kg.msg.controller.f;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.bean.mongo.Page;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.msg.bean.http.HBMessageTextBasic;
import hb.kg.msg.bean.http.HBSiteMailBasic;
import hb.kg.msg.bean.mongo.HBMessageText;
import hb.kg.msg.bean.mongo.HBSiteMail;
import hb.kg.msg.service.MessageTextService;
import hb.kg.msg.service.SiteMailService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 系统公告
 */
@Api(description = "[F]系统公告")
@RestController
@RequestMapping(value = { "/${api.version}/f/messagetext" })
public class MessageFTextController extends BaseCRUDController<HBMessageText> {
    @Autowired
    private MessageTextService messageTextService;
    @Autowired
    private SiteMailService siteMailService;

    @Override
    protected BaseCRUDService<HBMessageText> getService() {
        return messageTextService;
    }

    /**
     * 所有系统通知
     */
    @ApiOperation(value = "获取所有系统通知", notes = "获取所有系统通知", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/allsysmsg", method = { RequestMethod.POST })
    public ResponseBean getAllsysmsg(@RequestBody HBMessageTextBasic allmail) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            query.addCriteria(new Criteria().andOperator(Criteria.where("type")
                                                                 .ne("priletter")
                                                                 .andOperator(Criteria.where("fromValid")
                                                                                      .is(true))));
            if (allmail.getPage() != null) {
                List<Order> orders = new ArrayList<Order>(); // 排序
                orders.add(new Order(Direction.DESC, "createTime"));
                Sort sort = Sort.by(orders);
                allmail.getPage().setSort(sort);
                Long count = mongoTemplate.count(query, HBMessageTextBasic.class);
                allmail.getPage().setTotalSize(count.intValue());
                query.with(allmail.getPage());
            }
            List<HBMessageTextBasic> mts = mongoTemplate.find(query, HBMessageTextBasic.class);// 查询所以的可见的系统通知
            for (HBMessageTextBasic mt : mts) {
                List<HBSiteMailBasic> mail = mongoTemplate.find(new Query(Criteria.where("to")
                                                                                  .is(userid)
                                                                                  .and("messagetext")
                                                                                  .is(mt.getId())),
                                                                HBSiteMailBasic.class);
                if (mail.size() == 0) {// 如果没找到，说明此系统通知用户未读
                    mt.setIsRead(false);
                } else {
                    mt.setIsRead(true);
                }
            }
            allmail.getPage().setList(mts);
            responseBean.setData(allmail.getPage());
        }
        return returnBean(responseBean);
    }

    /**
     * 当前用户查看新系统通知
     * @param object
     * @return
     */
    @ApiOperation(value = "当前用户查看新系统通知", notes = "当前用户查看新系统通知", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/show", method = { RequestMethod.POST })
    public ResponseBean showMessage(@RequestBody HBMessageTextBasic object) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            List<HBMessageTextBasic> messagetexts = new ArrayList<HBMessageTextBasic>();// 新通知消息列表
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("type")
                                                                 .is("global")
                                                                 .andOperator(Criteria.where("fromValid")
                                                                                      .is(true))));
            List<HBMessageTextBasic> wr = mongoTemplate.find(query, HBMessageTextBasic.class);// 查询所以的可见的系统通知
            for (HBMessageTextBasic messagetext : wr) {
                // 遍历所有的hb_site_mails，根据系统通知id、当前用户、消息已读条件
                List<HBSiteMailBasic> mail = mongoTemplate.find(new Query(Criteria.where("to")
                                                                                  .is(userid)
                                                                                  .and("messagetext")
                                                                                  .is(messagetext.getId())
                                                                                  .and("isRead")
                                                                                  .is(true)),
                                                                HBSiteMailBasic.class);
                if (mail.size() == 0) {// 如果没找到，说明此系统通知用户未读
                    messagetexts.add(messagetext);// 当前用户未读的系统通知
                }
            }
            Page<HBMessageTextBasic> page = object.getPage();
            if (page != null && messagetexts != null) {
                int totalcount = messagetexts.size(); // 集合长度
                if (totalcount > 0) {
                    int pagesize = page.getPageSize();// 每页数量
                    int pagenumber = page.getPageNumber();// 第几页
                    int m = totalcount % pagesize; // 最后一页的数据条数
                    int pagecount = 0; // 总共几页
                    page.setTotalSize(totalcount); // 数据总条数
                    page.setPageNumber(pagenumber);// 第几页
                    page.setPageSize(pagesize);// 每页几个
                    pagecount = m > 0 ? totalcount / pagesize + 1 : totalcount / pagesize;
                    page.setTotalPage(pagecount); // 总共几页
                    page.setTotalPage(pagecount); // 总共几页
                    List<HBMessageTextBasic> subList = pagenumber == pagecount
                            ? messagetexts.subList((pagenumber - 1) * pagesize, totalcount)
                            : messagetexts.subList((pagenumber - 1) * pagesize,
                                                   pagesize * pagenumber);
                    page.setList(subList);
                }
                responseBean.setData(page);
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 公告详情
     */
    @ApiOperation(value = "公告详情", notes = "用户查看公告详情，如果公告原为未读则将状态改为已读", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/showmore/{id}", method = { RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean viewNewSiteMail(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(id)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("id").is(id), // 查看公告详细信息
                                                         Criteria.where("fromValid").is(true)));
            HBMessageText messagetext = mongoTemplate.findOne(query, HBMessageText.class);
            if (messagetext != null) {
                HBSiteMail mail = mongoTemplate.findOne(new Query(Criteria.where("to")// 查看hb_site_mails是否有此条记录
                                                                          .is(userid)
                                                                          .and("messagetext")
                                                                          .is(id)
                                                                          .and("isRead")
                                                                          .is(true)),
                                                        HBSiteMail.class);
                if (mail == null) {// 为null说明用户对此消息未读状态，那么就插入一条已读公告
                    HBSiteMail sitemail = new HBSiteMail();
                    sitemail.setMessagetext(messagetext);
                    sitemail.setIsRead(true);
                    sitemail.setTitle(messagetext.getTitle());
                    sitemail.setTo(new HBUserBasic(userid));
                    sitemail.setType("global");
                    sitemail.prepareHBBean();
                    siteMailService.dao().insert(sitemail);
                }
                responseBean.setData(messagetext);
            } else {
                responseBean.setData(messagetext);
                responseBean.setCode(ApiCode.SUCCESS.getCode());
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 复杂查询，没必要写到dao中，因为肯定只有这里会用
     */
    @ApiOperation(value = "得到系统公告-复杂查询", notes = "得到系统公告-复杂查询，包含已读、删除、所有", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/load", method = { RequestMethod.POST })
    public ResponseBean getContentById(@RequestBody HBSiteMail mail) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else if (mail.getType() != null) {
            // 查看已读系统公告
            query.addCriteria(new Criteria().andOperator(Criteria.where("to").is(userid),
                                                         Criteria.where("type").is(mail.getType()),
                                                         Criteria.where("isRead").is(true)));
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
