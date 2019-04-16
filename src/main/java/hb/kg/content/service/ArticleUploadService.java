package hb.kg.content.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.content.bean.http.HBArticleUpload;
import hb.kg.content.dao.ArticleUploadDao;

@Service
public class ArticleUploadService extends BaseCRUDService<HBArticleUpload> {
    @Resource
    private ArticleUploadDao articleUploadDao;

    @Override
    public BaseDao<HBArticleUpload> dao() {
        return articleUploadDao;
    }
}
