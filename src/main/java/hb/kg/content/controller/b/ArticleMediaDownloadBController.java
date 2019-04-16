package hb.kg.content.controller.b;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.content.bean.mongo.HBArticleMediaDownload;
import hb.kg.content.service.ArticleMediaDownloadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(description = "[B]文章内容里的图片等上传管理")
@RestController
@RequestMapping(value = { "/${api.version}/b/articlemedia" })
public class ArticleMediaDownloadBController extends BaseCRUDController<HBArticleMediaDownload> {
    @Autowired
    private ArticleMediaDownloadService articleMediaDownloadService;

    @Override
    protected BaseCRUDService<HBArticleMediaDownload> getService() {
        return articleMediaDownloadService;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticleMediaDownload object) {
        return super.query(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBArticleMediaDownload object) {
        return super.update(object);
    }

    /**
     * 文件上传阿里云OSS
     */
    @RequestMapping(value = "/uploadoss", method = { RequestMethod.POST })
    public ResponseBean uploadoos(@ApiParam("文件") @RequestPart(value = "file") MultipartFile file) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        String originalFilename = file.getOriginalFilename(); // 上传时的文件名
        if (StringUtils.isNotEmpty(userid) && StringUtils.isNotEmpty(originalFilename)
                && file.getSize() > 1) {
            try {
                responseBean.setData(articleMediaDownloadService.uploadMediaToOss(file, userid));
            } catch (Exception e) {
                logger.error("上传文件失败：" + originalFilename, e);
                responseBean.setErrMsg("文件上传失败");
                responseBean.setCode(ApiCode.INTERNAL_ERROR.getCode());
            }
        } else {
            responseBean.setErrMsg("用户没有登录或者文件小于2k");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        }
        return returnBean(responseBean);
    }

    /**
     * OSS删除文件
     */
    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        HBArticleMediaDownload hbDownload = articleMediaDownloadService.dao().findOne(id);
        if (HBStringUtil.isNotBlank(hbDownload.getTitle())) {
            articleMediaDownloadService.deleteOSSFile(hbDownload.getTitle());
        }
        return super.prepareRemove(id, responseBean);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

    /**
     * 复杂查询，没必要写到dao中，因为肯定只有这里会用
     */
    @RequestMapping(value = "/complexquery", method = { RequestMethod.POST })
    public ResponseBean getContentById(@ApiParam("查询条件") @RequestBody HBArticleMediaDownload searhBean) {
        ResponseBean responseBean = getReturn();
        Query query = new Query();
        if (searhBean.getId() != null) {
            query.addCriteria(Criteria.where("id").is(searhBean.getId()));
        }
        if (searhBean.getFilename() != null) {
            query.addCriteria(Criteria.where("filename").regex(searhBean.getFilename()));
        }
        if (searhBean.getFiletype() != null) {
            query.addCriteria(Criteria.where("filetype").is(searhBean.getFiletype()));
        }
        if (searhBean.getListCreateTime() != null && searhBean.getListCreateTime().size() == 2
                && searhBean.getListCreateTime().get(0) != null
                && searhBean.getListCreateTime().get(1) != null) {
            query.addCriteria(Criteria.where("createTime")
                                      .gte(searhBean.getListCreateTime().get(0))
                                      .lte(searhBean.getListCreateTime().get(1)));
        }
        if (searhBean.getAuthor() != null && searhBean.getAuthor().getId() != null) {
            query.addCriteria(Criteria.where("author").is(searhBean.getAuthor().getId()));
        }
        if (searhBean.getPage() != null) {
            List<Order> orders = new ArrayList<Order>(); // 排序
            orders.add(new Order(Direction.DESC, "createTime"));
            Sort sort = Sort.by(orders);
            searhBean.getPage().setSort(sort);
            Long count = mongoTemplate.count(query, HBArticleMediaDownload.class);
            searhBean.getPage().setTotalSize(count.intValue());
            query.with(searhBean.getPage());
        }
        List<HBArticleMediaDownload> list = mongoTemplate.find(query, HBArticleMediaDownload.class);
        searhBean.getPage().setList(list);
        responseBean.setData(searhBean.getPage());
        return returnBean(responseBean);
    }
}
