package hb.kg.content.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.content.bean.http.HBArticleUpload;

@Repository("articleUploadDao")
public class ArticleUploadDao extends BaseMongoDao<HBArticleUpload> {
}
