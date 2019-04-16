package hb.kg.upload.controller.b;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.dao.BaseDao;
import hb.kg.content.bean.http.HBArticleUpload;
import hb.kg.content.dao.ArticleUploadDao;
import hb.kg.upload.bean.mongo.HBTempMongoBean;
import hb.kg.upload.controller.HBTempBaseController;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.Api;

/**
 * excel解析
 */
@Api(description = "[B]条款上传")
@RestController
@RequestMapping(value = "/${api.version}/b/excel/article")
public class ArticleBUploadController extends HBTempBaseController<HBArticleUpload> {
    @SuppressWarnings("unused")
    private static String qa_excel_warn_msg = "excel解析出了问题，请检查excel格式，请求的格式应满足：\n1、标题   作者  发文日期    来源  正文内容    摘要  分类  备注标签\n"
            + "\n2、第一行应该是表格头，不是表格正文内容";
    @Resource
    private ArticleUploadDao articleUploadDao;

    @Override
    protected BaseDao<HBArticleUpload> getDao() {
        return articleUploadDao;
    }

    @Override
    protected boolean parseInsertRow(Row row,
                                     List<String> srcIdList,
                                     HashMap<String, HBTempMongoBean> srcBeanList,
                                     String userid,
                                     String excelType,
                                     ResponseBean responseBean) {
        if (row.getLastCellNum() == 8) {
            HBArticleUpload hbar = new HBArticleUpload();
            hbar.setTitle(row.getCell(0).getStringCellValue());
            if (StringUtils.isNotEmpty(hbar.getTitle())) {
                hbar.setArticleauthor(row.getCell(1).getStringCellValue());
                hbar.setDate(row.getCell(2).getStringCellValue());
                hbar.setSource(row.getCell(3).getStringCellValue());
                hbar.setContent(row.getCell(4).getStringCellValue());
                hbar.setSummary(row.getCell(5).getStringCellValue());
                String[] categorys = row.getCell(6).getStringCellValue().split("，|,");
                List<String> categoryList = new ArrayList<>();
                Collections.addAll(categoryList, categorys);
                hbar.setCategorys(categoryList);
                hbar.setLabelother(row.getCell(7).getStringCellValue());
                HBUserBasic user = new HBUserBasic();
                user.setId(userid);
                hbar.setAuthor(user);
                hbar.prepareBaseBean();
                HBTempMongoBean pack = generateHBTempMongoBeanAritice(hbar, userid);
                srcBeanList.put(hbar.getId(), pack);
                srcIdList.add(hbar.getId());
            }
            return true;
        } else {
            return false;
        }
    }
}
