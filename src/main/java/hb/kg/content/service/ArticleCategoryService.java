package hb.kg.content.service;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.content.bean.mongo.HBArticleCategory;
import hb.kg.content.dao.ArticleCategoryDao;

@Service
public class ArticleCategoryService extends BaseCRUDService<HBArticleCategory> {
    @Resource
    private ArticleCategoryDao articleCategoryDao;

    @Override
    public BaseDao<HBArticleCategory> dao() {
        return articleCategoryDao;
    }

    public Map<String, HBArticleCategory> findInitResources() {
        Collection<HBArticleCategory> allData = articleCategoryDao.findAll();
        return articleCategoryDao.generateFatherAndChildRelation(allData);
//        Collection<HBArticleCategory> allData = articleCategoryDao.findAll();
//        if (HBCollectionUtil.isNotEmpty(allData)) {
//            return allData.stream()
//                          .collect(Collectors.toMap(HBArticleCategory::getId, Function.identity()));
//        } else {
//            return new HashMap<>();
//        }
        
    }

    public HBArticleCategory getATreedCategory(String id) {
        return articleCategoryDao.getBeanInTree(id);
    }

    public void recursiveDelete(String id) {
        articleCategoryDao.recursiveDelete(id, 2);
    }
}