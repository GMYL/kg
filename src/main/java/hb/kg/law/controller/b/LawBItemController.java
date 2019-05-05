package hb.kg.law.controller.b;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.law.bean.mongo.HBLawitem;
import hb.kg.law.service.LawItemService;
import io.swagger.annotations.Api;

@Api(description = "[B]法规条款管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/lawitem" })
public class LawBItemController extends BaseCRUDController<HBLawitem> {
    @Autowired
    private LawItemService lawitemService;

    @Override
    protected BaseCRUDService<HBLawitem> getService() {
        return lawitemService;
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
    public ResponseBean get(@PathVariable("id") String id) {
        return super.get(id);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBLawitem object) {
        return super.query(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBLawitem object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBLawitem object) {
        return super.update(object);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    @RequestMapping(value = "/all", method = { RequestMethod.DELETE })
    public ResponseBean removeAll(@RequestBody List<String> ids) {
        return super.removeAll(ids);
    }

    @RequestMapping(value = "/loadmany", method = { RequestMethod.POST })
    public ResponseBean loadmany(@RequestBody List<String> ids) {
        ResponseBean responseBean = getReturn();
        if (!CollectionUtils.isEmpty(ids)) {
            responseBean.setData(lawitemService.dao().findAll(ids));
        }
        return returnBean(responseBean);
    }
}
