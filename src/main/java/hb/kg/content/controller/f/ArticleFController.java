package hb.kg.content.controller.f;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.anno.RegisterMethod;
import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.content.bean.enums.ArticleStateEnum;
import hb.kg.content.bean.http.HBArticleForList;
import hb.kg.content.bean.http.HBArticleTalkBasic;
import hb.kg.content.bean.mongo.HBArticle;
import hb.kg.content.service.ArticleService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;

@Api(description = "[F]文章操作")
@RestController
@RequestMapping(value = { "/${api.version}/f/article" })
public class ArticleFController extends BaseCRUDController<HBArticle> {
    protected static String robot_password_parse = "小花瓣儿密码输入错误，请检查密码~";
    @Autowired
    private ArticleService articleService;

    @Override
    protected BaseCRUDService<HBArticle> getService() {
        return articleService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticle object) {
        return super.query(object);
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RegisterMethod
    public JSONObject getA() {
        JSONObject rsMap = new JSONObject();
        rsMap.put("a", "a");
        return rsMap;
    }

    @RegisterMethod
    public JSONObject asdasdasdas() {
        JSONObject rsMap = new JSONObject();
        rsMap.put("asd", "21312fsf");
        return rsMap;
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
            if (object.getContent().length() > 150) {
                object.setSummary(object.getContent().substring(0, 150));
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

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBArticle object) {
        return super.update(object);
    }

    @Override
    protected HBArticle prepareUpdate(HBArticle object,
                                      ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (userid == null || object.getAuthor() == null
                || !userid.equals(object.getAuthor().getId())) {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        } else {
            // 重新指派用户为当前用户
            HBUserBasic user = new HBUserBasic();
            user.setId(userid);
            object.setAuthor(user);
        }
        if (StringUtils.isEmpty(object.getSummary())
                && StringUtils.isNotEmpty(object.getContent())) {
            if (object.getContent().length() > 100) {
                object.setSummary(object.getContent().substring(0, 100));
            } else {
                object.setSummary(object.getContent());
            }
        }
        return super.prepareUpdate(object, responseBean);
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
                responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
                return returnBean(responseBean);
            } else if (!robotpasswordDecode.equals(mainServer.conf().getRobotPassword())) {
                responseBean.setCodeEnum(ApiCode.NO_AUTH);
                return returnBean(responseBean);
            } else {
                HBUserBasic user = new HBUserBasic();
                user.setId("huaban");
                object.setAuthor(user);
                object.setState(1);
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

    /**
     * 删除一条委托信息（假删除）
     */
    @RequestMapping(value = "/fakedel/{id}", method = { RequestMethod.DELETE })
    public ResponseBean fakedel(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (userid == null) {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        } else if (id == null) {
            responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
        } else {
            // 重新指派用户为当前用户
            Update update = new Update();
            update.set("state", ArticleStateEnum.DELETE.getIndex());
            Query query = new Query(Criteria.where("id")
                                            .is(id)
                                            .andOperator(Criteria.where("user").is(userid)));
            mongoTemplate.updateFirst(query, update, HBArticle.class);
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/myall", method = { RequestMethod.GET })
    public ResponseBean myall() {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (userid == null) {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        } else {
            Query query = new Query(Criteria.where("user").is(userid));
            mongoTemplate.find(query, HBArticle.class);
        }
        return returnBean(responseBean);
    }

    // 获取政策解读
    @RequestMapping(value = "/policy", method = { RequestMethod.POST })
    public ResponseBean getPolicy(@RequestBody HBArticleForList object) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("categorys").in("政策解读"));
        query.addCriteria(Criteria.where("state").is(2));
        if (object.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "id"));
            Sort sort = Sort.by(orders);
            object.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleForList.class);
            object.getPage().setTotalSize(count.intValue());
            query.with(object.getPage());
        }
        List<HBArticleForList> list = mongoTemplate.find(query, HBArticleForList.class);
        object.getPage().setList(list);
        responseBean.setData(object.getPage());
        return returnBean(responseBean);
    }

    // 获取最新政策解读
    @RequestMapping(value = "/newpolicy", method = { RequestMethod.POST })
    public ResponseBean getNewPolicy(@RequestBody HBArticleForList object) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("categorys").all(object.getCategorys()));
        query.addCriteria(Criteria.where("state").is(2));
        if (object.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "date"));
            Sort sort = Sort.by(orders);
            object.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleForList.class);
            object.getPage().setTotalSize(count.intValue());
            query.with(object.getPage());
        }
        List<HBArticleForList> list = mongoTemplate.find(query, HBArticleForList.class);
        object.getPage().setList(list);
        responseBean.setData(object.getPage());
        return returnBean(responseBean);
    }

    // 获取最热点专题
    @RequestMapping(value = "/newhot", method = { RequestMethod.POST })
    public ResponseBean getNewhot(@RequestBody HBArticleForList object) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("categorys").regex("热点专题-主站"));
        query.addCriteria(Criteria.where("state").is(2));
        if (object.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "createTime"));
            Sort sort = Sort.by(orders);
            object.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleForList.class);
            object.getPage().setTotalSize(count.intValue());
            query.with(object.getPage());
        }
        List<HBArticleForList> list = mongoTemplate.find(query, HBArticleForList.class);
        object.getPage().setList(list);
        responseBean.setData(object.getPage());
        return returnBean(responseBean);
    }

    // 点击量排名相关文章
    @RequestMapping(value = "/clicktalk/{category}", method = { RequestMethod.GET })
    public ResponseBean getClicktalk(@PathVariable("category") String category) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("categorys").in(category));
        query.addCriteria(Criteria.where("state").is(2));
        query.with(Sort.by(new Order(Direction.DESC, "visitNum")));
        query.limit(5);
        List<HBArticleTalkBasic> list = mongoTemplate.find(query, HBArticleTalkBasic.class);
        responseBean.setData(list);
        return returnBean(responseBean);
    }

    // 最新文章
    @RequestMapping(value = "/newest", method = { RequestMethod.POST })
    public ResponseBean getNewest(@RequestBody HBArticleForList object) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("state").is(2));
        if (object.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 按照时间排序
            orders.add(new Order(Direction.DESC, "date"));
            Sort sort = Sort.by(orders);
            object.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleForList.class);
            object.getPage().setTotalSize(count.intValue());
            query.with(object.getPage());
        }
        List<HBArticleForList> list = mongoTemplate.find(query, HBArticleForList.class);
        object.getPage().setList(list);
        responseBean.setData(object.getPage());
        return returnBean(responseBean);
    }

    // 获得房地产子站热点专题，每个专题返回5条最新专题
    @RequestMapping(value = "/realty/getHot/{category}", method = { RequestMethod.GET })
    public ResponseBean getHot(@PathVariable("category") String category) {
        ResponseBean responseBean = getReturn();
        responseBean.setData(articleService.getListRealtyHot(category));
        return returnBean(responseBean);
    }

    /**
     * 复杂查询，没必要写到dao中，因为肯定只有这里会用
     */
    @RequestMapping(value = "/complexquery", method = { RequestMethod.POST })
    public ResponseBean getContentById(@RequestBody HBArticleForList searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        if (searhBean.getId() != null) {
            query.addCriteria(Criteria.where("id").is(searhBean.getId()));
        }
        if (searhBean.getCategorys() != null && searhBean.getCategorys().size() > 0) {
            query.addCriteria(Criteria.where("categorys").all(searhBean.getCategorys()));
        }
        if (searhBean.getTaxTag() != null && searhBean.getTaxTag().size() > 0) {
            query.addCriteria(Criteria.where("taxTag").in(searhBean.getTaxTag()));
        }
        if (searhBean.getRegionTag() != null && searhBean.getRegionTag().size() > 0) {
            query.addCriteria(Criteria.where("regionTag").in(searhBean.getRegionTag()));
        }
        if (searhBean.getState() != null && searhBean.getState() != -1) {
            query.addCriteria(Criteria.where("state").is(searhBean.getState()));
        }
        if (searhBean.getAuthor() != null && searhBean.getAuthor().getId() != null) {
            query.addCriteria(Criteria.where("author").is(searhBean.getAuthor().getId()));
        }
        if (searhBean.getArticleauthor() != null) {
            query.addCriteria(Criteria.where("articleauthor").is(searhBean.getArticleauthor()));
        }
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "date"));// 发文时间降序
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticle.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBArticle> list = mongoTemplate.find(query, HBArticle.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }
}
