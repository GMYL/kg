package hb.kg.search.controller.b;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.search.bean.enums.HBDictionaryExcelType;
import hb.kg.search.bean.mongo.HBDictionaryStopWord;
import hb.kg.search.service.DictionaryStopWordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 停词词典维护
 */
@Api(description = "[B]停词词典维护")
@RestController
@RequestMapping(value = { "/${api.version}/b/stopdict" })
public class DictionaryStopWordBController extends BaseCRUDController<HBDictionaryStopWord> {
    @Autowired
    private DictionaryStopWordService dictionaryStopWordService;

    @Override
    protected BaseCRUDService<HBDictionaryStopWord> getService() {
        return dictionaryStopWordService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBDictionaryStopWord object) {
        return super.query(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBDictionaryStopWord object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBDictionaryStopWord object) {
        return super.update(object);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/all", method = { RequestMethod.DELETE })
    public ResponseBean removeAll(@RequestBody List<String> ids) {
        return super.removeAll(ids);
    }

    @Override
    protected HBDictionaryStopWord prepareUpdate(HBDictionaryStopWord object,
                                                 ResponseBean responseBean) {
        object.setUpdateTime(new Date());
        return super.prepareUpdate(object, responseBean);
    }

    /**
     * 按照类型全部删除
     */
    @ApiOperation(value = "按照类型全部删除", notes = "按照类型全部删除", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/deltype/{type}", method = { RequestMethod.DELETE })
    public ResponseBean fakedel(@ApiParam(value = "停词词典类型") @PathVariable("type") String type) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(type)) {
            switch (HBDictionaryExcelType.valueOf(type)) {
            case WENDA:
            case FAGUI:
            case WENZHANG:
                Query query = new Query(Criteria.where("excelType").is(type));
                mongoTemplate.remove(query, HBDictionaryStopWord.class);
                responseBean.setData("删除成功");
                break;
            default:
                responseBean.setErrMsg("没有传来正确的类型");
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                break;
            }
        }
        return returnBean(responseBean);
    }
}
