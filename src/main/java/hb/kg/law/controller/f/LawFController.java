package hb.kg.law.controller.f;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.encrypt.MD5Util;
import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.law.controller.LawFBaseController;
import hb.kg.law.service.LawService;
import hb.kg.wizard.bean.mongo.HBGraphLaw;
import io.swagger.annotations.Api;

@Api(description = "[F]法规管理")
@RestController
@RequestMapping(value = { "/${api.version}/f/law" })
public class LawFController extends LawFBaseController<HBLaw> {
    @Autowired
    private LawService lawService;

    @Override
    protected BaseCRUDService<HBLaw> getService() {
        return lawService;
    }

    // 假删除
    @RequestMapping(value = "/test", method = { RequestMethod.GET })
    public ResponseBean test() {
        ResponseBean responseBean = getReturn();
        int insertSize = 1000;
        HashMap<String, HBLaw> insertLawMap = new HashMap<>(insertSize * 2); // 每次插入1000条，但考虑到0.75的扩展问题
        HBLaw law = getService().dao().findOne("F0072");
        String idname = MD5Util.getRandomMD5Code(law.getId() + law.getName());
        String idc = MD5Util.getRandomMD5Code(law.getId() + law.getContents());
        // insertLawMap.put(law.getId(), law);
        // HBLaw law2 = mongoTemplate.findOne(Query.query(Criteria.where("_id")
        // .in(insertLawMap.keySet())),
        // HBLaw.class);
        // law.setId("122222");
        // insertLawMap.clear();
        // insertLawMap.put(law.getId(), law);
        // HBLaw law3 = mongoTemplate.findOne(Query.query(Criteria.where("_id")
        // .in(insertLawMap.keySet())),
        // HBLaw.class);
        responseBean.setData(law);
        return returnBean(responseBean);
    }
}
