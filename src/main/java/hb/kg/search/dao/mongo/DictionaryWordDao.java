package hb.kg.search.dao.mongo;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.search.bean.mongo.HBDictionaryWord;

@Repository("dictionaryWordDao")
public class DictionaryWordDao extends BaseMongoDao<HBDictionaryWord> {
}
