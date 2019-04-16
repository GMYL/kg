package hb.kg.content.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseRedisMongoCacheDao;
import hb.kg.content.bean.mongo.HBArticle;

/**
 * 文章比较大，用redis进行缓存
 */
@Repository("articleDao")
public class ArticleDao extends BaseRedisMongoCacheDao<HBArticle> {
}
