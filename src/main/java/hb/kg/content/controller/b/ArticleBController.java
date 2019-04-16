package hb.kg.content.controller.b;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.content.bean.http.HBArticleForList;
import hb.kg.content.bean.http.HBArticleMultiSearch;
import hb.kg.content.bean.mongo.HBArticle;
import hb.kg.content.service.ArticleService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;

@Api(description = "[B]文章管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/article" })
public class ArticleBController extends BaseCRUDController<HBArticle> {
    protected static String robot_password_parse = "小花瓣儿密码输入错误，请检查密码~";
    @Autowired
    private ArticleService articleService;

    @Override
    protected BaseCRUDService<HBArticle> getService() {
        return articleService;
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBArticle object) {
        return super.update(object);
    }

    /**
     * 添加一条信息
     */
    @Override
    protected HBArticle prepareInsert(HBArticle object,
                                      ResponseBean responseBean) {
        if (StringUtils.isEmpty(object.getTitle())) {
            responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
            return super.prepareInsert(object, responseBean);
        }
        // 不允许以别的用户的名义进行insert
        String userid = getUseridFromRequest();
        HBUserBasic user = new HBUserBasic();
        user.setId(userid != null ? userid : "匿名用户");
        object.setAuthor(user);
        object.prepareBaseBean();
        if (StringUtils.isEmpty(object.getSummary())
                && StringUtils.isNotEmpty(object.getContent())) {
            if (object.getContent().length() > 100) {
                object.setSummary(object.getContent().substring(0, 100));
            } else {
                object.setSummary(object.getContent());
            }
        }
        return super.prepareInsert(object, responseBean);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBArticle object) {
        return super.insert(object);
    }

    /**
     * 爬虫上传文章
     */
    @RequestMapping(value = "robotput", method = { RequestMethod.PUT })
    public ResponseBean robotput(@RequestHeader("robot-password") String robotpassword,
                                 @RequestBody HBArticle object) {
        ResponseBean responseBean = getReturn();
        try {
            String robotpasswordDecode = URLDecoder.decode(robotpassword, "utf-8");
            if (StringUtils.isEmpty(object.getTitle())) {
                responseBean.setCodeEnum(ApiCode.NO_AUTH);
                return returnBean(responseBean);
            } else if (!robotpasswordDecode.equals(mainServer.conf().getRobotPassword())) {
                responseBean.setCodeEnum(ApiCode.NO_AUTH);
                return returnBean(responseBean);
            } else {
                HBUserBasic user = new HBUserBasic();
                user.setId("12479083317000");
                object.setAuthor(user);
                object.prepareBaseBean();
                if (StringUtils.isEmpty(object.getSummary())
                        && StringUtils.isNotEmpty(object.getContent())) {
                    if (object.getContent().length() > 100) {
                        object.setSummary(object.getContent().substring(0, 100));
                    } else {
                        object.setSummary(object.getContent());
                    }
                }
                getService().dao().insert(object);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("excep parse error!", e);
            responseBean.setErrMsg(robot_password_parse);
        }
        return returnBean(responseBean);
    }

    // 假删除
    @RequestMapping(value = "/fakedel/{id}", method = { RequestMethod.DELETE })
    public ResponseBean fakedel(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        Map<String, Object> params = new HashMap<>();
        params.put("state", 3);
        articleService.dao().updateOne(id, params);
        return returnBean(responseBean);
    }

    // 假批量删除
    @RequestMapping(value = "/fakedellot", method = { RequestMethod.POST })
    public ResponseBean fakedellot(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        articleService.fakeDelALot(ids);
        return returnBean(responseBean);
    }

    @Override
    protected List<String> prepareQueryFields(HBArticle object,
                                              ResponseBean responseBean) {
        if (object.getState() == -1) {
            object.setState(null);
        }
        List<String> fields = super.prepareQueryFields(object, responseBean);
        fields.remove("content");
        return fields;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticle object) {
        return super.query(object);
    }

    /**
     * 按照id获取内容
     */
    @RequestMapping(value = "/content/{id}", method = { RequestMethod.GET })
    public ResponseBean getContentById(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (!StringUtils.isEmpty(id)) {
            responseBean.setData(articleService.findContentById(id));
        }
        return returnBean(responseBean);
    }

    /**
     * 复杂查询，没必要写到dao中，因为肯定只有这里会用
     */
    @RequestMapping(value = "/complexquery", method = { RequestMethod.POST })
    public ResponseBean getContentById(@RequestBody HBArticleMultiSearch searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        if (searhBean.getId() != null) {
            query.addCriteria(Criteria.where("id").is(searhBean.getId()));
        }
        if (searhBean.getAuthor().getId() != null) {
            query.addCriteria(Criteria.where("author.$id").is(searhBean.getAuthor().getId()));
        }
        if (searhBean.getTitle() != null) {
            query.addCriteria(Criteria.where("title").regex(searhBean.getTitle()));
        }
        if (searhBean.getCategorys() != null && searhBean.getCategorys().size() > 0) {
            query.addCriteria(Criteria.where("categorys").in(searhBean.getCategorys()));
        }
        if (searhBean.getState() != null && searhBean.getState() != -1) {
            query.addCriteria(Criteria.where("state").is(searhBean.getState()));
        }
        if (searhBean.getDate() != null) {
            query.addCriteria(Criteria.where("date").is(searhBean.getDate()));
        }
        if (searhBean.getArticleauthor() != null) {
            query.addCriteria(Criteria.where("articleauthor").is(searhBean.getArticleauthor()));
        }
        if (searhBean.getCreateTime() != null && searhBean.getCreateTime().size() == 2
                && searhBean.getCreateTime().get(0) != null
                && searhBean.getCreateTime().get(1) != null) {
            query.addCriteria(Criteria.where("createTime")
                                      .gte(searhBean.getCreateTime().get(0))
                                      .lte(searhBean.getCreateTime().get(1)));
        }
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "date"));
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleForList.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBArticleForList> list = mongoTemplate.find(query, HBArticleForList.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }
}
