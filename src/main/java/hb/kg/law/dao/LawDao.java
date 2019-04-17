package hb.kg.law.dao;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.law.bean.mongo.HBLaw;

@Repository("lawDao")
public class LawDao extends BaseMongoDao<HBLaw> {
	@Override
	public HBLaw insert(HBLaw object) {
		object.prepareHBBean();
		return super.insert(object);
	}

	@Override
	public Collection<HBLaw> insertAll(Collection<HBLaw> objects) {
		if (CollectionUtils.isNotEmpty(objects)) {
			for (HBLaw law : objects) {
				law.prepareHBBean();
			}
		}
		return super.insertAll(objects);
	}

}
