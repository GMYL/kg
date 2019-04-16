package hb.kg.content.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseTreeLocalMongoCacheDao;
import hb.kg.content.bean.mongo.HBArticleCategory;

/**
 * 文章分类少且小，直接全量加载到内存即可
 */
@Repository("articleCategoryDao")
public class ArticleCategoryDao extends BaseTreeLocalMongoCacheDao<HBArticleCategory> {
}
