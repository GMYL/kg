package hb.kg.search.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.search.bean.mongo.HBDictionaryStopWord;
import hb.kg.search.dao.mongo.DictionaryStopWordDao;

@Service
public class DictionaryStopWordService extends BaseCRUDService<HBDictionaryStopWord> {
    @Resource
    private DictionaryStopWordDao dictionaryStopWordDao;

    @Override
    public BaseDao<HBDictionaryStopWord> dao() {
        return dictionaryStopWordDao;
    }
}
