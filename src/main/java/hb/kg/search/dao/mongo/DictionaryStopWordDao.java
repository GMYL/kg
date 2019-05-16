package hb.kg.search.dao.mongo;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.search.bean.mongo.HBDictionaryStopWord;

@Repository("dictionaryStopWordDao")
public class DictionaryStopWordDao extends BaseMongoDao<HBDictionaryStopWord> {
}
