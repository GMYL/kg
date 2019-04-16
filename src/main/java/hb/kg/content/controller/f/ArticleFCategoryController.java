package hb.kg.content.controller.f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.content.bean.mongo.HBArticleCategory;
import hb.kg.content.service.ArticleCategoryService;
import io.swagger.annotations.Api;

@Api(description = "[F]文章类别")
@RestController
@RequestMapping(value = { "/${api.version}/f/articlecategory" })
public class ArticleFCategoryController extends BaseCRUDController<HBArticleCategory> {
    @Autowired
    private ArticleCategoryService articleCategoryService;

    @Override
    protected BaseCRUDService<HBArticleCategory> getService() {
        return articleCategoryService;
    }

    /**
     * 获取后台初始化系统需要用到的标签 默认获取2层的标签
     */
    @RequestMapping(value = "/init", method = { RequestMethod.GET })
    public ResponseBean getInitSysTags() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(articleCategoryService.findInitResources());
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/tree/{id}", method = { RequestMethod.GET })
    public ResponseBean getATreedTag(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        responseBean.setData(articleCategoryService.getATreedCategory(id));
        return returnBean(responseBean);
    }

    /**
     * 根据父类标签获取子标签列表
     */
    @RequestMapping(value = "/parent/{parent}", method = { RequestMethod.GET })
    public ResponseBean getParent(@PathVariable("parent") String parent) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotEmpty(parent)) {
            Map<String, Object> params = new HashMap<>();
            params.put("parent", parent);
            List<HBArticleCategory> categorys = mongoTemplate.find(Query.query(Criteria.where("parent").is(parent)), HBArticleCategory.class);
            if (categorys.size() != 0) {
                List<String> list = new ArrayList<>();
                for (HBArticleCategory hbArticleCategory : categorys) {
                    list.add(hbArticleCategory.getId());
                }
                responseBean.setData(list);
                return returnBean(responseBean);
            }
            responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
            return returnBean(responseBean);
        }
        responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
        return returnBean(responseBean);
    }
}
