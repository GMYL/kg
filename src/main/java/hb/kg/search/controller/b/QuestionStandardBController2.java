package hb.kg.search.controller.b;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.search.bean.mongo.HBQuestionStandard2;
import hb.kg.search.service.QuestionStandardService2;
import io.swagger.annotations.Api;

@Api(description = "[B]标准问答查询")
@RestController
@RequestMapping(value = { "/${api.version}/b/questand2" })
public class QuestionStandardBController2 extends BaseCRUDController<HBQuestionStandard2> {
    @Autowired
    private QuestionStandardService2 questionStandardService2;

    @Override
    protected BaseCRUDService<HBQuestionStandard2> getService() {
        return questionStandardService2;
    }

    /**
     * 利用当前数据库内的标题生成暗标题
     */
    @RequestMapping(value = "/map/generateMapDarkTitle", method = { RequestMethod.GET })
    public ResponseBean generateMapDarkTitle() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(questionStandardService2.generateDarkTitle());
        return returnBean(responseBean);
    }
}
