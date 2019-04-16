package hb.kg.upload.controller;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.KeyValuePair;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseCRUDController;
import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.office.OfficeUtil;
import hb.kg.upload.bean.ExcelUploadBean;
import hb.kg.upload.bean.mongo.HBTempMongoBean;
import hb.kg.upload.dao.TempMongoDao;
import hb.kg.upload.service.TempMongoService;
import hb.kg.user.bean.http.HBUserBasic;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@SuppressWarnings({ "unchecked" })
@RestController
public abstract class HBTempBaseController<K extends ExcelUploadBean<K>>
        extends BaseCRUDController<HBTempMongoBean> {
    protected static String excel_type_parse = "excel文件输入格式有误，请检查文件输入格式";
    @Resource
    protected TempMongoDao tempMongoDao;
    @Autowired
    protected TempMongoService tempMongoService;

    @Override
    protected BaseCRUDService<HBTempMongoBean> getService() {
        return tempMongoService;
    }

    protected abstract BaseDao<K> getDao();

    protected Class<K> getClassT() {
        return getDao().getClassT();
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBTempMongoBean object) {
        return super.query(object);
    }

    private HBTempMongoBean getBean(HBTempMongoBean obj) {
        return (HBTempMongoBean) obj;
    }

    public List<K> getAllSrc(List<HBTempMongoBean> beans) {
        List<K> rsList = new ArrayList<>();
        for (HBTempMongoBean tempBean : beans) {
            try {
                K HBTempMongoBean = (K) tempBean.getSrcBean();
                HBTempMongoBean.setExcelType(tempBean.getExcelType());
                rsList.add(HBTempMongoBean);
            } catch (Exception e) {
            }
        }
        return rsList;
    }

    /**
     * 让子类进行覆盖
     */
    protected abstract boolean parseInsertRow(Row row,
                                              List<String> srcIdList,
                                              HashMap<String, HBTempMongoBean> srcBeanList,
                                              String userid,
                                              String excelType,
                                              ResponseBean responseBean);

    protected HashMap<String, Object> doParseAndInsert(Workbook wb,
                                                       String userid,
                                                       String excelType,
                                                       ResponseBean responseBean) {
        // 获取sheet数目
        HashMap<String, Object> rsMap = new HashMap<>();
        List<Integer> failRow = new ArrayList<>();
        int successNum = 0;
        int failNum = 0;
        for (int HBTempMongoBean = 0; HBTempMongoBean < wb.getNumberOfSheets(); HBTempMongoBean++) {
            Sheet sheet = wb.getSheetAt(HBTempMongoBean);
            Row row = null;
            int lastRowNum = sheet.getLastRowNum();
            // 循环读取
            List<String> srcIdList = new ArrayList<>();
            HashMap<String, HBTempMongoBean> srcBeanList = new HashMap<>();
            for (int i = 1; i <= lastRowNum; i++) {
                try {
                    row = sheet.getRow(i);
                    if (row != null) {
                        if (parseInsertRow(row,
                                           srcIdList,
                                           srcBeanList,
                                           userid,
                                           excelType,
                                           responseBean)) {
                            successNum++;
                        } else {
                            failNum++;
                            failRow.add(i + 1);
                        }
                    }
                } catch (Exception e) {
                    logger.error("excep parse error!", e);
                    responseBean.setErrMsg(excel_type_parse);
                    failNum++;
                    failRow.add(i + 1);
                }
            }
            tempMongoService.removeExistings(userid, srcIdList);
            // 如果是扩展词典不用插入中间数据库
            if (getClassT().getName().equals("hb.kg.search.bean.mongo.HBDictionaryWord")) {
                List<HBTempMongoBean> beans = new ArrayList<>();
                for (HBTempMongoBean tempBean : srcBeanList.values()) {
                    beans.add(getBean(tempBean));
                }
                List<K> srcBeans = getAllSrc(beans);
                if (CollectionUtils.isNotEmpty(srcBeans)) {
                    getDao().insertAll(srcBeans);
                }
            } else {
                for (HBTempMongoBean tempBean : srcBeanList.values()) {
                    tempMongoDao.insert(getBean(tempBean));
                }
            }
        }
        rsMap.put("插入成功", successNum);
        rsMap.put("插入失败", failNum);
        rsMap.put("失败行号", failRow);
        return rsMap;
    }

    @ApiOperation(value = "上传文件", notes = "上传文件", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/upload")
    public ResponseBean uploadLaw(@ApiParam("Excel文件类型") @RequestHeader("excel-type") String excelType,
                                  @ApiParam("单个文件名") @RequestPart(value = "file") MultipartFile file,
                                  @ApiParam("文件名列表") @RequestParam(value = "files") MultipartFile[] files) {
        ResponseBean responseBean = getReturn();
        try {
            // 读取Excel
            String userid = getUseridFromRequest();
            if (StringUtils.isEmpty(userid)) {
                responseBean.setCodeEnum(ApiCode.NO_AUTH);
            } else {
                Workbook wb = OfficeUtil.getWorkbookByFilename(file.getOriginalFilename(),
                                                               file.getInputStream());
                if (wb == null) {
                    responseBean.setErrMsg(excel_type_parse);
                } else {
                    String excelTypeDecode = URLDecoder.decode(excelType, "utf-8");
                    HashMap<String, Object> result = doParseAndInsert(wb,
                                                                      userid,
                                                                      excelTypeDecode,
                                                                      responseBean);
                    responseBean.setData(result);
                }
            }
        } catch (Exception e) {
            logger.error("excep parse error!", e);
            responseBean.setErrMsg(excel_type_parse);
        }
        return returnBean(responseBean);
    }

    protected HBTempMongoBean generateHBTempMongoBean(Object object,
                                                      String userid,
                                                      String excelType) {
        HBTempMongoBean pack = new HBTempMongoBean();
        pack.setSrcBean(object);
        pack.setClazz(object.getClass().getName());
        pack.setExcelType(excelType);
        HBUserBasic user = new HBUserBasic();
        user.setId(userid);
        pack.setUser(user);
        pack.prepareHBBean();
        return (HBTempMongoBean) pack;
    }

    /**
     * 去除excelType的写入
     * @return
     */
    protected HBTempMongoBean generateHBTempMongoBeanAritice(Object object,
                                                             String userid) {
        HBTempMongoBean pack = new HBTempMongoBean();
        pack.setSrcBean(object);
        pack.setClazz(object.getClass().getName());
        HBUserBasic user = new HBUserBasic();
        user.setId(userid);
        pack.setUser(user);
        pack.setId(RandomStringUtils.random(9, true, true));
        pack.setCreateTime(new Date());
        return (HBTempMongoBean) pack;
    }

    @Override
    protected HBTempMongoBean prepareQuery(HBTempMongoBean object,
                                           ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isNotEmpty(userid)) {
            getBean(object).setClazz(getClassT().getName());
            getBean(object).setUser(new HBUserBasic(userid));
        } else {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        }
        return super.prepareQuery(object, responseBean);
    }

    @Override
    protected HBTempMongoBean prepareUpdate(HBTempMongoBean tempBean,
                                            ResponseBean responseBean) {
        try {
            JSONObject src = (JSONObject) getBean(tempBean).getSrcBean();
            K law = JSON.toJavaObject(src, getClassT());
            getBean(tempBean).setSrcBean(law);
        } catch (Exception e) {
            logger.error("json传递的有问题:" + tempBean);
            responseBean.setErrMsg("json传递的格式有问题");
            responseBean.setCode(ApiCode.PARAM_FORMAT_ERROR.getCode());
        }
        return super.prepareUpdate(tempBean, responseBean);
    }

    protected void endConfirm(List<K> srcBeans) {
    }

    /**
     * 确认问答内容是有效内容
     */
    @ApiOperation(value = "确认问答内容是有效内容", notes = "确认问答内容是有效内容", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/confirm", method = { RequestMethod.POST })
    public ResponseBean confirmQA(@ApiParam("信息") @RequestBody KeyValuePair<String, Object> kv) {
        ResponseBean responseBean = getReturn();
        try {
            String type = kv.getKey();
            if ("confirm-and-delete".equals(type)) {
                List<String> datas = (List<String>) kv.getValue();
                List<HBTempMongoBean> beans = tempMongoService.findManyByIdAndRemove(datas);
                if (beans != null && beans.size() > 0) {
                    List<K> srcBeans = getAllSrc(beans);
                    if (CollectionUtils.isNotEmpty(srcBeans)) {
                        // 删掉旧的才能添加新的
                        List<String> idList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(srcBeans)) {
                            for (K baseBean : srcBeans) {
                                if (StringUtils.isNoneEmpty(baseBean.getId())) {
                                    idList.add(baseBean.getId());
                                }
                            }
                        }
                        getDao().removeAll(idList);
                        getDao().insertAll(srcBeans);
                        endConfirm(srcBeans);
                        responseBean.setData("插入操作已完成");
                    }
                } else {
                    responseBean.setData("没有查询到这部分数据");
                }
            } else if ("only-delete".equals(type)) {
                List<String> datas = (List<String>) kv.getValue();
                List<HBTempMongoBean> beans = tempMongoService.findManyByIdAndRemove(datas);
                if (beans != null && beans.size() > 0) {
                    List<K> srcBeans = getAllSrc(beans);
                    if (CollectionUtils.isNotEmpty(srcBeans)) {
                        // 删掉
                        List<String> idList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(srcBeans)) {
                            for (K baseBean : srcBeans) {
                                if (StringUtils.isNoneEmpty(baseBean.getId())) {
                                    idList.add(baseBean.getId());
                                }
                            }
                        }
                        getDao().removeAll(idList);
                        responseBean.setData("删除操作已完成");
                    }
                } else {
                    responseBean.setData("没有查询到这部分数据");
                }
            } else if ("confirm-all-ByExcelType-and-delete".equals(type)) {
                List<HBTempMongoBean> beans = mongoTemplate.findAllAndRemove(Query.query(Criteria.where("excelType")
                                                                                                 .is(kv.getValue())),
                                                                             HBTempMongoBean.class);
                if (beans != null && beans.size() > 0) {
                    List<K> srcBeans = getAllSrc(beans);
                    if (CollectionUtils.isNotEmpty(srcBeans)) {
                        // 删掉旧的才能添加新的
                        List<String> idList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(srcBeans)) {
                            for (K baseBean : srcBeans) {
                                if (StringUtils.isNoneEmpty(baseBean.getId())) {
                                    idList.add(baseBean.getId());
                                }
                            }
                        }
                        getDao().removeAll(idList);
                        getDao().insertAll(srcBeans);
                        endConfirm(srcBeans);
                        responseBean.setData("插入操作已完成");
                    }
                } else {
                    responseBean.setData("没有查询到这部分数据");
                }
            }
        } catch (Exception e) {
            logger.error("excep parse error!", e);
            responseBean.setErrMsg(excel_type_parse);
        }
        return returnBean(responseBean);
    }

    /**
     * 文件上传，在后台打开
     */
    @ApiOperation(value = "文件解析", notes = "文件解析。文件上传，在后台打开", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/parse")
    public ResponseBean parse(@ApiParam("Excel文件类型") @RequestHeader("excel-type") String excelType,
                              @ApiParam("单个文件名") @RequestPart(value = "file") MultipartFile file,
                              @ApiParam("文件名列表") @RequestParam(value = "files") MultipartFile[] files) {
        ResponseBean responseBean = getReturn();
        try {
            // 读取Excel
            Workbook wb = OfficeUtil.getWorkbookByFilename(file.getOriginalFilename(),
                                                           file.getInputStream());
            if (wb == null) {
                responseBean.setErrMsg(excel_type_parse);
            } else {
                // 获取sheet数目
                for (int HBTempMongoBean = 0; HBTempMongoBean < wb.getNumberOfSheets(); HBTempMongoBean++) {
                    Sheet sheet = wb.getSheetAt(HBTempMongoBean);
                    Row row = null;
                    int lastRowNum = sheet.getLastRowNum();
                    // 循环读取
                    for (int i = 0; i <= lastRowNum; i++) {
                        row = sheet.getRow(i);
                        if (row != null) {
                            // 获取每一列的值
                            for (int j = 0; j < row.getLastCellNum(); j++) {
                                Cell cell = row.getCell(j);
                                String value = OfficeUtil.getCellValue(cell);
                                if (!value.equals("")) {
                                    System.out.print(value + " | ");
                                }
                            }
                            System.out.println();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("excep parse error!", e);
        }
        return returnBean(responseBean);
    }

    public String getCellStringValue(Row row,
                                     int col) {
        return getCellStringValue(row, col, 0);
    }

    public String getCellStringValue(Row row,
                                     int col,
                                     int decimal) {
        Cell cell = row.getCell(col);
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.format("%." + decimal + "f", cell.getNumericCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
            }
        }
        return null;
    }
}
