package hb.kg.law.controller.b;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.law.service.LawService;
import io.swagger.annotations.Api;

@Api(description = "[B]法规管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/law" })
public class LawBController extends BaseCRUDController<HBLaw> {
	@Autowired
	private LawService lawService;

	@Override
	protected BaseCRUDService<HBLaw> getService() {
		return lawService;
	}

	@RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
	public ResponseBean get(@PathVariable("id") String id) {
		return super.get(id);
	}

	@RequestMapping(value = "/query", method = { RequestMethod.POST })
	public ResponseBean query(@RequestBody HBLaw object) {
		return super.query(object);
	}

	@RequestMapping(value = "", method = { RequestMethod.PUT })
	public ResponseBean insert(@RequestBody HBLaw object) {
		return super.insert(object);
	}

	@RequestMapping(value = "/update", method = { RequestMethod.POST })
	public ResponseBean update(@RequestBody HBLaw object) {
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

	// 假删除
	@RequestMapping(value = "/fakedel/{id}", method = { RequestMethod.DELETE })
	public ResponseBean fakedel(@PathVariable("id") String id) {
		ResponseBean responseBean = getReturn();
		Map<String, Object> params = new HashMap<>();
		params.put("state", false);
		lawService.dao().updateOne(id, params);
		return returnBean(responseBean);
	}

	@Override
	protected HBLaw prepareQuery(HBLaw object, ResponseBean responseBean) {
		object.setState(true);
		return super.prepareQuery(object, responseBean);
	}
}
