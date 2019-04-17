package hb.kg.law.controller.f;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.service.BaseCRUDService;
import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.law.controller.LawFBaseController;
import hb.kg.law.service.LawService;
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

}
