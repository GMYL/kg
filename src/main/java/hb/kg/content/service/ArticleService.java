package hb.kg.content.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.content.bean.http.HBArticleBasic;
import hb.kg.content.bean.http.HBArticleCategorys;
import hb.kg.content.bean.http.HBArticleContent;
import hb.kg.content.bean.http.HBArticleMore;
import hb.kg.content.bean.http.HBArticleTalkBasic;
import hb.kg.content.bean.mongo.HBArticle;
import hb.kg.content.bean.mongo.HBArticleCategory;
import hb.kg.content.dao.ArticleCategoryDao;
import hb.kg.content.dao.ArticleDao;

@Service
public class ArticleService extends BaseCRUDService<HBArticle> {
    @Resource
    private ArticleDao articleDao;
    @Resource
    private ArticleCategoryDao articleCategoryDao;

    public BaseDao<HBArticle> dao() {
        return articleDao;
    }

    public HBArticleContent findContentById(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where("id").is(id)),
                                     HBArticleContent.class);
    }

    public boolean fakeDelALot(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            Query query = new Query(Criteria.where("id").in(ids));
            Update update = new Update();
            update.set("state", 3);
            mongoTemplate.updateMulti(query, update, HBArticle.class);
            return true;
        } else {
            return false;
        }
    }

    public void addArticleclickNum(HBArticleMore object) {
        if (object.getVisitNum() == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("visitNum", 1);
            articleDao.updateOne(object.getId(), params);
        } else {
            object.setVisitNum(object.getVisitNum() + 1);
            Map<String, Object> params = new HashMap<>();
            params.put("visitNum", object.getVisitNum());
            articleDao.updateOne(object.getId(), params);
        }
    }

    /**
     * 获取当前文章作者下其他最新文章
     */
    public List<HBArticleTalkBasic> getRelateArticles(String author,
                                                      int size) {
        Query query = new Query(Criteria.where("articleauthor")
                                        .is(author)
                                        .andOperator(Criteria.where("state").is(2)));
        query.skip(1).limit(size);// 从第一条开始记录，返回size条数据
        query.with(Sort.by(new Order(Direction.DESC, "date")));
        List<HBArticleTalkBasic> articles = mongoTemplate.find(query, HBArticleTalkBasic.class);
        return articles;
    }

    public void replaceCategorys() {
        List<String> root = new ArrayList<>();
        root.add("首页新闻");
        root.add("热点专题");
        root.add("名家税谈");
        for (String r : root) {
            Query query = new Query(Criteria.where("categorys").regex(r));
            List<HBArticleCategorys> listArticles = mongoTemplate.find(query,
                                                                       HBArticleCategorys.class);
            for (HBArticleCategorys article : listArticles) {
                List<String> categorys = article.getCategorys();
                List<String> newCategorys = new ArrayList<String>();
                if (categorys != null & categorys.size() > 0) {
                    String s = "";
                    for (String category : categorys) {
                        if (category.indexOf("主站") < 0 && category.indexOf("房地产平台") < 0) {
                            String[] arryString = category.split("-", 2);
                            if (arryString.length > 1) {
                                s = arryString[0] + "-主站-" + arryString[1];
                            } else {
                                s = arryString[0];
                            }
                            if (s != "") {
                                newCategorys.add(s);
                            }
                        } else {
                            newCategorys.add(category);
                        }
                    }
                }
                if (newCategorys.size() > 0) {
                    // System.out.println(newCategorys);
                    article.setCategorys(newCategorys);
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", article.getId());
                    params.put("categorys", article.getCategorys());
                    // System.out.println(params);
                    articleDao.updateOne(article.getId(), params);
                }
            }
        }
    }

    public List<JSONObject> getListRealtyHot(String category) {
        List<JSONObject> listJson = new ArrayList<>();
        List<HBArticleCategory> listCategory = mongoTemplate.find(new Query(Criteria.where("parent")
                                                                                    .is("热点专题-"
                                                                                            + category)),
                                                                  HBArticleCategory.class);
        for (HBArticleCategory articleCategory : listCategory) {
            Query query = new Query(Criteria.where("categorys")
                                            .is(articleCategory.getId())
                                            .andOperator(Criteria.where("state").is(2)));
            query.limit(5);// 从第一条开始记录，返回size条数据
            query.with(Sort.by(new Order(Direction.DESC, "date")));
            List<HBArticleBasic> listHotArticle = mongoTemplate.find(query, HBArticleBasic.class);
            JSONObject jsobj = new JSONObject();
            jsobj.put("category", articleCategory.getId());
            jsobj.put("body", listHotArticle);
            listJson.add(jsobj);
        }
        return listJson;
    }
}