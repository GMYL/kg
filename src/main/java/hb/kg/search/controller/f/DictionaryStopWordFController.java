package hb.kg.search.controller.f;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.search.bean.mongo.HBDictionaryStopWord;
import hb.kg.search.service.DictionaryStopWordService;
import io.swagger.annotations.Api;

/**
 * 停词词典维护前台接口阻塞
 */
@Api(description = "[F]停词词典维护")
@RestController
@RequestMapping(value = { "/${api.version}/f/stopdict" })
public class DictionaryStopWordFController extends BaseCRUDController<HBDictionaryStopWord> {
    @Autowired
    private DictionaryStopWordService dictionaryStopWordService;

    @Override
    protected BaseCRUDService<HBDictionaryStopWord> getService() {
        return dictionaryStopWordService;
    }
    
    
}
