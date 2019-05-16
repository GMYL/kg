package hb.kg.system.controller.b;

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
import hb.kg.system.bean.mongo.HBModuleCategory;
import hb.kg.system.dao.ModuleCategoryDao;
import hb.kg.system.service.ModuleCategoryService;
import io.swagger.annotations.Api;

/**
 * 模块管理
 */
@Api(description = "[B]模块管理")
@RestController
@RequestMapping(value = "/${api.version}/b/modulecategory")
public class ModuleBCategoryController extends BaseCRUDController<HBModuleCategory> {
    @Resource
    private ModuleCategoryDao moduleCategoryDao;
    @Autowired
    private ModuleCategoryService moduleCategoryService;

    @Override
    protected BaseCRUDService<HBModuleCategory> getService() {
        return moduleCategoryService;
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBModuleCategory object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBModuleCategory object) {
        return super.query(object);
    }

    /**
     * 获取后台初始化系统需要用到的标签 默认获取2层的标签
     */
    @RequestMapping(value = "/init", method = { RequestMethod.GET })
    public ResponseBean getInitSysTags() {
        ResponseBean responseBean = getReturn();
        Map<String, HBModuleCategory> initData = moduleCategoryDao.getAllTree();
        responseBean.setData(initData);
        return returnBean(responseBean);
    }

    @Override
    protected HBModuleCategory prepareInsert(HBModuleCategory moduleCategory,
                                             ResponseBean responseBean) {
        if (moduleCategory != null && StringUtils.isEmpty(moduleCategory.getId())) {
            responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
        }
        if (moduleCategory.getParent() != null && moduleCategory.getParent() != ""
                && !moduleCategory.getParent().equals("root")) {
            moduleCategory.setId(moduleCategory.getParent() + "-" + moduleCategory.getId());
        }
        return super.prepareInsert(moduleCategory, responseBean);
    }

    /*
     * 获取所有系统模块数据
     */
    @RequestMapping(value = "/queryAll", method = { RequestMethod.GET })
    public ResponseBean queryAll() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(mongoTemplate.findAll(HBModuleCategory.class));
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/recursive/{id}", method = { RequestMethod.DELETE })
    public ResponseBean recursiveDelete(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        id = prepareRemove(id, responseBean);
        if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
            moduleCategoryDao.recursiveDelete(id, 2);
        }
        endAllUpdate(null, responseBean);
        return returnBean(responseBean);
    }
}
