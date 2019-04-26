package hb.kg.wizard.controller.b;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.wizard.bean.enums.HBGraphNodeType;
import hb.kg.wizard.bean.mongo.HBGraphBaseNode;
import hb.kg.wizard.bean.mongo.HBGraphLaw;
import hb.kg.wizard.service.GraphClueService;
import hb.kg.wizard.service.GraphLinkService;
import hb.kg.wizard.service.GraphNodeService;
import io.swagger.annotations.Api;

/**
 * 后台的图谱通用对象，可以管理所有的东西
 */
@Api(description = "[B]后台管理图谱单词")
@RestController
@RequestMapping(value = { "/${api.version}/b/wizard/node", "/${api.version}/b/wizard/word" })
public class GraphNodeBController extends BaseCRUDController<HBGraphBaseNode> {
    @Autowired
    private GraphNodeService graphNodeService;
    @Autowired
    private GraphLinkService graphLinkService;
    @Autowired
    private GraphClueService graphClueService;

    @Override
    protected BaseCRUDService<HBGraphBaseNode> getService() {
        return graphNodeService;
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBGraphBaseNode object) {
        return super.update(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBGraphBaseNode object) {
        return super.insert(object);
    }

    /**
     * 对所有节点进行准备性插入
     */
    @Override
    protected HBGraphBaseNode prepareInsert(HBGraphBaseNode node,
                                            ResponseBean responseBean) {
        HBGraphBaseNode resultBean = node;
        // 先检查插入节点的类型，必须有类型，否则插入失败
        if (StringUtils.isNotBlank(node.getType())) {
            try {
                switch (HBGraphNodeType.valueOfName(node.getType())) {
                case WORD:
                    // resultBean = HBGraphBaseNode.fromBaseNode(node, HBGraphWord.class);
                    break;
                case LAW:
                    resultBean = HBGraphLaw.fromBaseNode(node);
                    break;
                case LAWITEM:
                    // resultBean = HBGraphBaseNode.fromBaseNode(node, HBGraphLawItem.class);
                    break;
                case NORMAL:
                default:
                    // 保留传过来的类型
                    break;
                }
            } catch (Exception e) {
                logger.error("用户传入的数据进行类型转换时出错", e);
                responseBean.setCodeAndErrMsg(ApiCode.PARAM_FORMAT_ERROR.getCode(),
                                              "用户传入的数据进行类型转换时出错");
            }
        } else {
            responseBean.setCodeAndErrMsg(ApiCode.PARAM_FORMAT_ERROR.getCode(), "缺少节点类型");
        }
        return super.prepareInsert(resultBean, responseBean);
    }

    /**
     * 获取将单词导入机器人词库的进度
     */
    @RequestMapping(value = "/getRestoreProgress", method = { RequestMethod.GET })
    public ResponseBean getRestoreProgress() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(graphNodeService.getRestoreInProgress());
        return returnBean(responseBean);
    }

    /**
     * 按照传来的id列表加载节点和边，只解决单词的扩充
     */
    @RequestMapping(value = "/getNodeAndLinkByIdList", method = { RequestMethod.POST })
    public ResponseBean getNodeAndLinkByIdList(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        if (CollectionUtils.isNotEmpty(ids)) {
            responseBean.setData(graphNodeService.getNodeAndLinkByIdList(ids));
        }
        return returnBean(responseBean);
    }

    /**
     * 按照一个节点查找它的临边和对应的点
     * @param id 传过来点的id
     * @param level 需要扩充的点扩充几层，默认一层
     */
    @RequestMapping(value = "/getNodeAndLinksById", method = { RequestMethod.GET })
    public ResponseBean getNodeAndLinksById(@RequestParam("id") String id,
                                            @RequestParam("level") Integer level) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(id)) {
            // responseBean.setData(graphNodeService.getNodeAndLinksById(id,
            // level != null && level > 0
            // ? level
            // : 1));
        }
        return returnBean(responseBean);
    }

    /**
     * 删掉一个节点与它相关的所有临边，保留这个节点
     */
    @RequestMapping(value = "/deleteAllLinkByNode", method = { RequestMethod.DELETE })
    public ResponseBean getNodeAndLinksById(@RequestParam("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isNotBlank(id)) {
            graphLinkService.deleteAllLinkByNode(id);
        }
        return returnBean(responseBean);
    }

    /**
     * 删掉一个节点以及与它相关的所有临边
     */
    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        if (StringUtils.isNotBlank(id)) {
            // 首先删掉所有有关的临边
            graphLinkService.deleteAllLinkByNode(id);
        }
        return super.prepareRemove(id, responseBean);
    }

    /**
     * 手动将法规导入知识图谱内，默认不清楚就有数据而是覆盖
     */
    @RequestMapping(value = "/man/loadLawDataIntoGraph", method = { RequestMethod.GET })
    public ResponseBean loadLawDataIntoGraph(@RequestParam Boolean clearAll) {
        ResponseBean responseBean = getReturn();
        responseBean.setData(graphNodeService.loadLawDataIntoGraph(clearAll == null ? false
                : clearAll));
        return returnBean(responseBean);
    }
}
