package hb.kg.wizard.controller.b;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import hb.kg.wizard.bean.mongo.HBGraphLink;
import hb.kg.wizard.service.GraphLinkService;
import io.swagger.annotations.Api;

/**
 * 后台管理图谱连接
 */
@Api(description = "[B]后台管理图谱连接")
@RestController
@RequestMapping(value = { "/${api.version}/b/wizard/link" })
public class GraphLinkBController extends BaseCRUDController<HBGraphLink> {
    @Autowired
    private GraphLinkService graphLinkService;

    @Override
    protected BaseCRUDService<HBGraphLink> getService() {
        return graphLinkService;
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBGraphLink object) {
        return super.update(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBGraphLink object) {
        return super.insert(object);
    }

    @Override
    protected HBGraphLink prepareInsert(HBGraphLink link,
                                        ResponseBean responseBean) {
        if (StringUtils.isBlank(link.getStart()) || StringUtils.isBlank(link.getEnd())) {
            responseBean.setCodeAndErrMsg(ApiCode.PARAM_FORMAT_ERROR.getCode(), "没有传入起点和终点");
        } else {
            // 强制让连线id为一个节点指向另一个节点
            if (mongoTemplate.exists(Query.query(Criteria.where("encrypt")
                                                         .in(link.generateEncrypt(link.getStart(),
                                                                                  link.getEnd()),
                                                             link.generateEncrypt(link.getEnd(),
                                                                                  link.getStart()))),
                                     HBGraphLink.class)) {
                responseBean.setCodeAndErrMsg(ApiCode.PARAM_CONTENT_ERROR.getCode(),
                                              "两个节点之间的连线已存在");
            } 
        }
        return super.prepareInsert(link, responseBean);
    }

    /**
     * 交换一个边的起点和终点
     */
    @RequestMapping(value = "/reverseLink/{id}", method = { RequestMethod.GET })
    public ResponseBean reverseLink(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(id)) {
            responseBean.setData(graphLinkService.reverseLink(id));
        }
        return returnBean(responseBean);
    }

    /**
     * 给定几个节点，删掉这几个节点之间所有相关边
     */
    @RequestMapping(value = "/deleteAllRelateLinks", method = { RequestMethod.POST })
    public ResponseBean deleteAllRelateLinks(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        if (CollectionUtils.isNotEmpty(ids)) {
            responseBean.setData(graphLinkService.deleteAllRelateLinks(ids));
        }
        return returnBean(responseBean);
    }
    
}
