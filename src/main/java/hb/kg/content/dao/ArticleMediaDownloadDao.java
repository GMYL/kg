package hb.kg.content.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.content.bean.mongo.HBArticleMediaDownload;

@Repository("articleMediaDownloadDao")
public class ArticleMediaDownloadDao extends BaseMongoDao<HBArticleMediaDownload> {
}
