package hb.kg.content.controller.b;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.content.bean.mongo.HBArticleCategory;
import hb.kg.content.dao.ArticleCategoryDao;
import hb.kg.content.service.ArticleCategoryService;
import io.swagger.annotations.Api;

@Api(description = "[B]文章类别管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/articlecategory" })
public class ArticleBCategoryController extends BaseCRUDController<HBArticleCategory> {
    @Resource
    private ArticleCategoryDao articleCategoryDao;
    @Autowired
    private ArticleCategoryService articleCategoryService;

    @Override
    protected BaseCRUDService<HBArticleCategory> getService() {
        return articleCategoryService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticleCategory object) {
        return super.query(object);
    }
    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }
    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBArticleCategory object) {
        return super.update(object);
    }
    
    /**
     * 获取后台初始化系统需要用到的标签 默认获取2层的标签
     */
    @RequestMapping(value = "/init", method = { RequestMethod.GET })
    public ResponseBean getInitSysTags() {
        ResponseBean responseBean = getReturn();
        Map<String, HBArticleCategory> initData = articleCategoryDao.getAllTree();
        responseBean.setData(initData);
        return returnBean(responseBean);
    }

    @Override
    protected HBArticleCategory prepareInsert(HBArticleCategory articleCategory,
                                              ResponseBean responseBean) {
        if (articleCategory != null && StringUtils.isEmpty(articleCategory.getId())) {
            responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
        }
        return super.prepareInsert(articleCategory, responseBean);
    }
    
    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBArticleCategory object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/recursive/{id}", method = { RequestMethod.DELETE })
    public ResponseBean recursiveDelete(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        id = prepareRemove(id, responseBean);
        if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
            articleCategoryService.recursiveDelete(id);
        }
        endAllUpdate(null, responseBean);
        return returnBean(responseBean);
    }
}
