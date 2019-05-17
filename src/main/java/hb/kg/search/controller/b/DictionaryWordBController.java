package hb.kg.search.controller.b;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
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
import hb.kg.search.bean.mongo.HBDictionaryWord;
import hb.kg.search.service.DictionaryWordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 词典维护
 */
@Api(description = "[B]词典维护")
@RestController
@RequestMapping(value = { "/${api.version}/b/dict" })
public class DictionaryWordBController extends BaseCRUDController<HBDictionaryWord> {
    @Autowired
    private DictionaryWordService dictionaryWordService;

    @Override
    protected BaseCRUDService<HBDictionaryWord> getService() {
        return dictionaryWordService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBDictionaryWord object) {
        return super.query(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBDictionaryWord object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBDictionaryWord object) {
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
    protected HBDictionaryWord prepareUpdate(HBDictionaryWord object,
                                             ResponseBean responseBean) {
        object.setUpdateTime(new Date());
        return super.prepareUpdate(object, responseBean);
    }

    /**
     * 手动触发更新内存词典树
     */
    @ApiOperation(value = "手动触发更新内存词典树", notes = "手动触发更新内存词典树", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/refreshDictionary", method = { RequestMethod.GET })
    public ResponseBean refreshDictionary() {
        ResponseBean responseBean = getReturn();
        dictionaryWordService.init();
        responseBean.setData("更新成功");
        return returnBean(responseBean);
    }

    /**
     * 获取词在问题标题中出现次数的排行
     */
    @ApiOperation(value = "获取词在问题标题中出现次数的排行", notes = "获取词在问题标题中出现次数的排行", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/getWordRankingByOccur", method = { RequestMethod.POST })
    public ResponseBean getContentById(@RequestBody HBDictionaryWord searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query(Criteria.where("occurNum").ne(0));
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "occurNum"));
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBDictionaryWord.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBDictionaryWord> list = mongoTemplate.find(query, HBDictionaryWord.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }

    /**
     * 按照类型全部删除
     */
    @ApiOperation(value = "按照类型全部删除", notes = "按照类型全部删除", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/deltype/{type}", method = { RequestMethod.DELETE })
    public ResponseBean fakedel(@ApiParam(value = "词典类型") @PathVariable("type") String type) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(type)) {
            switch (HBDictionaryExcelType.valueOf(type)) {
            case WENDA:
            case FAGUI:
            case WENZHANG:
                Query query = new Query(Criteria.where("excelType").is(type));
                mongoTemplate.remove(query, HBDictionaryWord.class);
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

    /**
     * 手动进行其他操作
     */
    @ApiOperation(value = "手动进行其他操作", notes = "手动进行其他操作", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/manual/{type}", method = { RequestMethod.GET })
    public ResponseBean manual(@ApiParam(value = "操作类型") @PathVariable("type") String type) {
        ResponseBean responseBean = getReturn();
        switch (type) {
        case "synonyms":
            // 计算同义词
            responseBean.setData(dictionaryWordService.loadSynonymsDicByPath("C:\\Users\\git\\ansj_seg\\library\\synonyms.dic"));
            break;
        case "nature":
            // 计算词性
            responseBean.setData(dictionaryWordService.loadNatureDicByPath("C:\\Users\\git\\ansj_seg\\library\\default.dic"));
            break;
        default:
            responseBean.setErrMsg("没有传来正确的类型");
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            break;
        }
        return returnBean(responseBean);
    }
    // @ApiOperation(value = "返回机器人智能问答生成的term", notes = "返回机器人智能问答生成的term",
    // produces = "application/json")
    // @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType =
    // "String", name = "hbjwtauth", value = "用户权限验证") })
    // @RequestMapping(value = "/termAnalysis", method = { RequestMethod.GET })
    // @ResponseStatus(HttpStatus.OK)
    // public ResponseBean termAnalysisBasic(@ApiParam(value = "问题", required =
    // true) @RequestParam("question") String question,
    // @ApiParam(value = "扩展", required = true) @RequestParam("expand") Boolean
    // expand) {
    // ResponseBean responseBean = getReturn();
    // if (expand) {
    // responseBean.setData(robotService.getQuestionTermsWithExpand(question));
    // } else {
    // responseBean.setData(new
    // ArrayList<>(robotService.getQuestionTermsBasic(question)
    // .values()));
    // }
    // return returnBean(responseBean);
    // }
}
