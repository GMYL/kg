package hb.kg.content.controller.b;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseBeanCRUDController;
import hb.kg.content.bean.http.HBArticleBasicList;
import io.swagger.annotations.Api;

@Api(description = "[B]文章基本列表管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/articlelist" })
public class ArticleBasicListBController extends BaseBeanCRUDController<HBArticleBasicList> {
    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticleBasicList object) {
        return super.query(object);
    }
}
