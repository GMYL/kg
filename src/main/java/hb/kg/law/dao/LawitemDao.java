package hb.kg.law.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.law.bean.mongo.HBLawitem;

@Repository("lawitemDao")
public class LawitemDao extends BaseMongoDao<HBLawitem> {
    public HashMap<String, String> getMany(List<String> ids) {
        HashMap<String, String> lawItems = new HashMap<String, String>();
        if (ids != null) {
            for (String lawitemId : ids) {
                HBLawitem hbli = findOne(lawitemId);
                if (hbli != null) {
                    lawItems.put(hbli.getId(), hbli.getItem());
                }
            }
        }
        return lawItems;
    }
}
