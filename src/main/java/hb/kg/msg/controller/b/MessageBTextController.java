package hb.kg.msg.controller.b;

import java.util.List;

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
import hb.kg.msg.bean.mongo.HBMessageText;
import hb.kg.msg.service.MessageTextService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;

@Api(description = "[B]系统公告管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/messagetext" })
public class MessageBTextController extends BaseCRUDController<HBMessageText> {
    @Autowired
    private MessageTextService messageTextService;

    @Override
    protected BaseCRUDService<HBMessageText> getService() {
        return messageTextService;
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/all", method = { RequestMethod.DELETE })
    public ResponseBean removeAll(@RequestBody List<String> ids) {
        return super.removeAll(ids);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBMessageText object) {
        return super.insert(object);
    }

    /**
     * 添加一条信息
     */
    @Override
    protected HBMessageText prepareInsert(HBMessageText object,
                                          ResponseBean responseBean) {
        if (StringUtils.isEmpty(object.getContent())) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("参数传递错误");
        } else {
            // 不允许以别的用户的名义进行insert
            String userid = getUseridFromRequest();
            if (StringUtils.isNotEmpty(userid)) {
                object.setFrom(new HBUserBasic(userid));
                object.prepareHBBean();
            } else {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("通知缺失信息");
            }
        }
        return super.prepareInsert(object, responseBean);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBMessageText object) {
        return super.query(object);
    }
}
