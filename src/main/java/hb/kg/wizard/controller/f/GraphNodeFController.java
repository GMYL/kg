package hb.kg.wizard.controller.f;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import hb.kg.wizard.service.GraphNodeService;
import io.swagger.annotations.Api;

/**
 * 后台管理图谱单词
 */
@Api(description = "[B]后台管理图谱单词")
@RestController
@RequestMapping(value = { "/${api.version}/f/wizard/node", "/${api.version}/f/wizard/word" })
public class GraphNodeFController extends BaseCRUDController<HBGraphBaseNode> {
    @Autowired
    private GraphNodeService graphNodeService;

    @Override
    protected BaseCRUDService<HBGraphBaseNode> getService() {
        return graphNodeService;
    }

    /**
     * 按照传来的id列表加载节点和边
     */
    @RequestMapping(value = "/getNodeAndLinkByIdList", method = { RequestMethod.POST })
    public ResponseBean getNodeAndLinkByIdList(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        if (CollectionUtils.isNotEmpty(ids)) {
            responseBean.setData(graphNodeService.getNodeAndLinkByIdList(ids));
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/getTermAndRank/{lawId}/{size}", method = { RequestMethod.GET })
    public ResponseBean getTermAndRank(@PathVariable("lawId") String lawId,
                                       @PathVariable("size") Integer size) {
        ResponseBean responseBean = getReturn();
        responseBean.setData(graphNodeService.getTermAndRank(lawId, size));
        return returnBean(responseBean);
    }

    /*
     * 数据导出
     */
    @RequestMapping(value = "/downFileTxt", method = { RequestMethod.POST })
    public ResponseBean createFileTxt(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        graphNodeService.createFileTxt(ids);
        responseBean.setData("http://www.aqielu.cn/kg/static/download/download/laws.txt");
        return returnBean(responseBean);
    }
}
