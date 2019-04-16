package hb.kg.content.controller.f;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseBeanCRUDController;
import hb.kg.content.bean.http.HBArticleForListNews;
import hb.kg.content.bean.mongo.HBArticle;
import io.swagger.annotations.Api;

@Api(description = "[F]文章列表")
@RestController
@RequestMapping(value = { "/${api.version}/f/articlelist" })
public class ArticleListFController extends BaseBeanCRUDController<HBArticleForListNews> {
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

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticleForListNews object) {
        return super.query(object);
    }
}
