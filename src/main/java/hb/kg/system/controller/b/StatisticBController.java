//package hb.kg.system.controller.b;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import hb.kg.common.bean.enums.ApiCode;
//import hb.kg.common.bean.http.ResponseBean;
//import hb.kg.common.controller.BaseCRUDController;
//import hb.kg.common.service.BaseCRUDService;
//import hb.kg.system.bean.enums.HBStatisticType;
//import hb.kg.system.bean.mongo.HBStatistic;
//import hb.kg.system.service.StatisticService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//
///**
// * 统计数据管理
// */
//@Api(description = "[B]统计数据管理")
//@RestController
//@RequestMapping(value = "/${api.version}/b/system/statistic")
//public class StatisticBController extends BaseCRUDController<HBStatistic> {
//    @Autowired
//    private StatisticService statisticService;
//
//    @Override
//    protected BaseCRUDService<HBStatistic> getService() {
//        return statisticService;
//    }
//
//    /**
//     * 按照数据类型，获取最新的一条统计数据
//     */
//    @ApiOperation(value = "获取信息", notes = "按照传入参数返回信息", produces = "application/json")
//    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
//    @RequestMapping(value = "/info", method = { RequestMethod.GET })
//    public ResponseBean info(@RequestParam("type") String type) {
//        ResponseBean responseBean = getReturn();
//        if (StringUtils.isNotBlank(type)) {
//            try {
//                switch (HBStatisticType.valueOf(type)) {
//                case QADIST: // 输入的是QADIST
//                    responseBean.setData(statisticService.getNewestData(HBStatisticType.QADIST.getName()));
//                    break;
//                case LAWDIST: // 输入的是LAWDIST
//                    responseBean.setData(statisticService.getNewestData(HBStatisticType.LAWDIST.getName()));
//                    break;
//                case ARTICLEDIST: // 输入的是ARTICLEDIST
//                    responseBean.setData(statisticService.getNewestData(HBStatisticType.ARTICLEDIST.getName()));
//                    break;
//                case GRAPHDIST: // 输入的是GRAPHDIST
//                    responseBean.setData(statisticService.getNewestData(HBStatisticType.GRAPHDIST.getName()));
//                    break;
//                case WORKFLOWDIST: // 输入的是WORKFLOWDIST
//                    responseBean.setData(statisticService.getNewestData(HBStatisticType.WORKFLOWDIST.getName()));
//                    break;
//                default:
//                    break;
//                }
//            } catch (Exception e) {
//                responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
//            }
//        }
//        return returnBean(responseBean);
//    }
//
//    /**
//     * 立刻生成统计数据
//     */
//    @RequestMapping(value = "/man/generateReport", method = { RequestMethod.GET })
//    public ResponseBean generateReport(@RequestParam("type") String type) {
//        ResponseBean responseBean = getReturn();
//        if (StringUtils.isNotBlank(type)) {
//            try {
//                responseBean.setData(statisticService.doGenerateStatistic(type));
//            } catch (Exception e) {
//                responseBean.setCodeEnum(ApiCode.PARAM_CONTENT_ERROR);
//            }
//        }
//        return returnBean(responseBean);
//    }
//}
