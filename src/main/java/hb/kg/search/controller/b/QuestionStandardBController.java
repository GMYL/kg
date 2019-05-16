package hb.kg.search.controller.b;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.common.util.time.TimeUtil;
import hb.kg.search.bean.mongo.HBQuestionStandard;
import hb.kg.search.service.QuestionStandardService;
import io.swagger.annotations.Api;

@Api(description = "[B]标准问答查询")
@RestController
@RequestMapping(value = { "/${api.version}/b/questand" })
public class QuestionStandardBController extends BaseCRUDController<HBQuestionStandard> {
    @Autowired
    private QuestionStandardService questionStandardService;

    @Override
    protected BaseCRUDService<HBQuestionStandard> getService() {
        return questionStandardService;
    }

    @RequestMapping(value = "/upsert", method = { RequestMethod.POST })
    public ResponseBean upsert(@RequestBody HBQuestionStandard object) {
        return super.upsert(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBQuestionStandard object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBQuestionStandard object) {
        return super.query(object);
    }

   
    @Override
    protected HBQuestionStandard prepareUpdate(HBQuestionStandard object,
                                               ResponseBean responseBean) {
        return super.prepareUpdate(object, responseBean);
    }

    @RequestMapping(value = "/findTitleKeyword", method = { RequestMethod.POST })
    public ResponseBean findTitleKeyword(@RequestBody HBQuestionStandard searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        if (searhBean.getId() != null) {
            query.addCriteria(Criteria.where("id").is(searhBean.getId()));
        }
        if (searhBean.getTitle() != null) {
            query.addCriteria(Criteria.where("title").regex(searhBean.getTitle()));
        }
        if (searhBean.getItems() != null && searhBean.getItems().size() > 0) {
            query.addCriteria(Criteria.where("items").all(searhBean.getItems()));
        }
        if (searhBean.getExcelType() != null) {
            query.addCriteria(Criteria.where("excelType").is(searhBean.getExcelType()));
        }
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "createTime"));
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBQuestionStandard.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBQuestionStandard> list = mongoTemplate.find(query, HBQuestionStandard.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }

    /**
     * 利用当前数据库内的标题生成暗标题
     */
    @RequestMapping(value = "/man/generateDarkTitle", method = { RequestMethod.GET })
    public ResponseBean generateDarkTitle() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(questionStandardService.generateDarkTitle());
        return returnBean(responseBean);
    }

}
