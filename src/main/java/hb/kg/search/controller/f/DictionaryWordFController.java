//package hb.kg.search.controller.f;
//
//import java.util.ArrayList;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//
//import hb.kg.common.bean.http.ResponseBean;
//import hb.kg.common.controller.BaseCRUDController;
//import hb.kg.common.service.BaseCRUDService;
//import hb.kg.search.bean.mongo.HBDictionaryWord;
//import hb.kg.search.service.DictionaryWordService;
//import hb.kg.search.service.RobotService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//
///**
// * 词典维护前台接口阻塞
// */
//@Api(description = "[F]词典维护")
//@RestController
//@RequestMapping(value = { "/${api.version}/f/dict" })
//public class DictionaryWordFController extends BaseCRUDController<HBDictionaryWord> {
//    @Autowired
//    private RobotService robotService;
//    @Autowired
//    private DictionaryWordService dictionaryWordService;
//
//    @Override
//    protected BaseCRUDService<HBDictionaryWord> getService() {
//        return dictionaryWordService;
//    }
//
//    @ApiOperation(value = "返回机器人智能问答生成的term", notes = "返回机器人智能问答生成的term", produces = "application/json")
//    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证") })
//    @RequestMapping(value = "/termAnalysis", method = { RequestMethod.GET })
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseBean termAnalysisBasic(@ApiParam(value = "问题", required = true) @RequestParam("question") String question,
//                                          @ApiParam(value = "扩展", required = true) @RequestParam("expand") Boolean expand) {
//        ResponseBean responseBean = getReturn();
//        if (expand) {
//            responseBean.setData(robotService.getQuestionTermsWithExpand(question));
//        } else {
//            responseBean.setData(new ArrayList<>(robotService.getQuestionTermsBasic(question)
//                                                             .values()));
//        }
//        return returnBean(responseBean);
//    }
//}
