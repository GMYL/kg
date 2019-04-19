package hb.kg.law.controller.f;

import java.util.HashMap;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.encrypt.MD5Util;
import hb.kg.law.bean.http.HBLawBasic;
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
//        int insertSize = 1000;
        CloseableIterator<HBLawBasic> itr = mongoTemplate.stream(new Query(Criteria.where("state")
                                                                             .is(true)),
                                                         HBLawBasic.class);
        int count = 0; //定义一个统计器
        while(itr.hasNext()){
            count++;
            itr.next();
            System.out.println(count);
        }
        itr.close();
        responseBean.setData(count);
        return returnBean(responseBean);
    }
}
