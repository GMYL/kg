package hb.kg.law.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.law.bean.mongo.HBLaw;
import hb.kg.law.dao.LawDao;

@Service
public class LawService extends BaseCRUDService<HBLaw> {
	@Resource
	private LawDao lawDao;

	public BaseDao<HBLaw> dao() {
		return lawDao;
	}

	public HBLaw insert(HBLaw object) {
		object.prepareHBBean();
		// 写mongo
		object = dao().insert(object);

		return object;
	}

	public Map<String, Object> getLawStatus() {
		Map<String, Object> rsMap = new HashMap<>();
		rsMap.put("totalCount", lawDao.count());
		return rsMap;
	}

	public boolean allDelLaw(List<String> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			lawDao.removeAll(ids);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 解决其它模块数据调用，这里可以加入截断，对用户的调用数据进行截断并本类的特有功能
	 */
	public Object solveData(JSONObject data) {
		return super.solveData(data);
	}
}
