//package hb.kg.system.service;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import javax.annotation.Resource;
//import javax.mail.internet.MimeMessage;
//
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.Sort.Direction;
//import org.springframework.data.domain.Sort.Order;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
//
//import com.redfin.sitemakgenerator.ChangeFreq;
//import com.redfin.sitemakgenerator.WebSitemakgenerator;
//import com.redfin.sitemakgenerator.WebSitemapUrl;
//
//import hb.kg.common.dao.BaseMongoDao;
//import hb.kg.common.server.QueueServer;
//import hb.kg.common.service.BaseCRUDService;
//import hb.kg.common.service.TimerService;
//import hb.kg.common.util.json.JSONObject;
//import hb.kg.common.util.time.TimeUtil;
//import hb.kg.content.bean.http.HBArticleSitemap;
//import hb.kg.law.bean.http.HBLawSitemap;
//import hb.kg.search.bean.http.HBQuestionStandardBasic;
//import hb.kg.search.bean.mongo.HBAnswerHistory;
//import hb.kg.system.bean.http.HBModuleVisitInfo;
//import hb.kg.system.bean.mongo.HBSystemInfo;
//import hb.kg.system.bean.mongo.HBVisitHistory;
//import hb.kg.system.bean.mongo.HBVisitStatus;
//import hb.kg.system.dao.SystemInfoDao;
//import hb.kg.user.bean.mongo.HBUser;
//import hb.kg.workflow.base.bean.enums.HBWorkflowStateEnum;
//import hb.kg.workflow.base.bean.mongo.WorkflowState;
//import hb.kg.workflow.qa.bean.enums.HBQuestionStateEnum;
//import hb.kg.workflow.qa.bean.mongo.HBQuestion;
//
//@Service
//public class SystemInfoService extends BaseCRUDService<HBSystemInfo> {
//    @Resource
//    private SystemInfoDao systemInfoDao;
//    @Resource
//    private MongoTemplate mongoTemplate;
//    @Autowired
//    private JavaMailSender mailSender;
//    @Autowired
//    private RequestMappingHandlerMapping requestMappingHandlerMapping;
//    @Autowired
//    private VisitStatusService visitStatusService;
//    @Autowired
//    private QueueServer queueServer;
//    @Autowired
//    private ModuleService moduleService;
//    @Autowired
//    private SystemRunningLogService systemRunningLogService;
//
//    public BaseMongoDao<HBSystemInfo> dao() {
//        return systemInfoDao;
//    }
//
//    public String parseHttpRequestPath(String path) {
//        if (StringUtils.isNotEmpty(path)) {
//            // 删掉最后一位然后再匹配
//            String[] urlSplit = path.split("/");
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < urlSplit.length - 1; i++) {
//                sb.append(urlSplit[i]).append("/");
//            }
//            path = sb.substring(0, sb.length() - 1);
//        }
//        return path;
//    }
//
//    public List<String> getAllPublicPath() {
//        List<String> rsList = new ArrayList<>();
//        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
//        for (Map.Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {
//            for (String url : m.getKey().getPatternsCondition().getPatterns()) {
//                if (url.contains("/{")) {
//                    rsList.add("/bk" + url.split("/\\{")[0]);
//                } else {
//                    rsList.add("/bk" + url);
//                }
//            }
//        }
//        return rsList;
//    }
//
//    public HBSystemInfo generateSystemInfo(String period) {
//        HBSystemInfo sysinfo = new HBSystemInfo();
//        switch (period) {
//        case "day":
//            sysinfo.setId(TimerService.now_to_day);
//            break;
//        case "hour":
//            sysinfo.setId(TimerService.now_to_hour);
//            break;
//        case "minute":
//        default:
//            sysinfo.setId(TimerService.now_to_min);
//            break;
//        }
//        sysinfo.setCreateTime(new Date());
//        sysinfo.setThreadNum(Thread.getAllStackTraces().size());
//        sysinfo.setJobQueueSize(queueServer.getQueueSize());
//        sysinfo.setPeriod(period);
//        return sysinfo;
//    }
//
//    /**
//     * 生成系统的信息，每10分钟生成一次
//     */
//    @Scheduled(cron = "0 */10 * * * *")
//    public HBSystemInfo saveSystemInfoMin() {
//        HBSystemInfo systemInfo = generateSystemInfo("minute");
//        if (sysConfService.getRunOnDev()) {
//            dao().insert(systemInfo, true);
//        }
//        return systemInfo;
//    }
//
//    /**
//     * 生成系统的信息，每小时生成一次
//     */
//    @Scheduled(cron = "0 2 * * * *")
//    public HBSystemInfo saveSystemInfoHour() {
//        HBSystemInfo systemInfo = generateSystemInfo("hour");
//        // 搜集上一小时所有用户的visit信息
//        String nowHour = TimerService.now_to_hour;
//        String searchHour = TimeUtil.getStringFromFreq(new Date(System.currentTimeMillis()
//                - 3600l * 1000), "hour");
//        Query query = new Query(Criteria.where("id").gt(searchHour).lt(nowHour));
//        query.addCriteria(Criteria.where("period").is("minute"));
//        List<HBVisitStatus> visitStatusList = mongoTemplate.find(query, HBVisitStatus.class);
//        List<HBModuleVisitInfo> moduleList = moduleService.getAllRecordingModule();
//        if (CollectionUtils.isNotEmpty(visitStatusList)) {
//            for (HBVisitStatus vs : visitStatusList) {
//                if (CollectionUtils.isNotEmpty(vs.getVisitList())) {
//                    for (HBVisitHistory history : vs.getVisitList()) {
//                        moduleList.stream()
//                                  .forEach(item -> moduleService.match(item,
//                                                                       history.getPath(),
//                                                                       history.getMethod()));
//                    }
//                }
//            }
//        }
//        systemInfo.setModuleVisit(moduleList);
//        if (sysConfService.getUseSystemLog()) {
//            dao().insert(systemInfo, true);
//        }
//        return systemInfo;
//    }
//
//    /**
//     * 生成系统的信息，每天1:05时生成一次
//     */
//    @Scheduled(cron = "0 5 1 * * *")
//    public HBSystemInfo saveSystemInfoDay() {
//        HBSystemInfo systemInfo = generateSystemInfo("day");
//        String nowDay = TimerService.now_to_day;
//        String searchDay = TimeUtil.getStringFromFreq(new Date(System.currentTimeMillis()
//                - 24l * 3600 * 1000), "day");
//        Query query = new Query(Criteria.where("id").gt(searchDay).lt(nowDay));
//        query.addCriteria(Criteria.where("period").is("hour"));
//        List<HBSystemInfo> systemInfos = mongoTemplate.find(query, HBSystemInfo.class);
//        List<HBModuleVisitInfo> moduleList = moduleService.getAllRecordingModule();
//        if (CollectionUtils.isNotEmpty(systemInfos) && CollectionUtils.isNotEmpty(moduleList)) {
//            Map<String, HBModuleVisitInfo> moduleMap = moduleList.stream()
//                                                                 .collect(Collectors.toMap(HBModuleVisitInfo::getId,
//                                                                                           Function.identity()));
//            for (HBSystemInfo si : systemInfos) {
//                if (si.getModuleVisit() != null) {
//                    si.getModuleVisit().stream().forEach(item -> {
//                        HBModuleVisitInfo visitInfo = moduleMap.get(item.getId());
//                        if (visitInfo != null) {
//                            visitInfo.setCount(visitInfo.getCount() + item.getCount());
//                        }
//                    });
//                }
//            }
//        }
//        systemInfo.setModuleVisit(moduleList);
//        if (sysConfService.getUseSystemLog()) {
//            dao().insert(systemInfo, true);
//            // 增加系统运行日志
//            systemRunningLogService.addSystemRunningLog(dao().getClassT(),
//                                                        "生成昨日的系统运行数据:" + systemInfo);
//        }
//        return systemInfo;
//    }
//
//    public Map<String, Object> getSystemInfoCount() {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("sumUser", mongoTemplate.count(new Query(), HBUser.class));
//        Query query = new Query();
//        Calendar cal = Calendar.getInstance();
//        {
//            cal.set(cal.get(Calendar.YEAR),
//                    cal.get(Calendar.MONDAY),
//                    cal.get(Calendar.DAY_OF_MONTH),
//                    0,
//                    0,
//                    0);
//            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//            query.addCriteria(Criteria.where("regDate")
//                                      .lt(new Date())
//                                      .gt(new Date(cal.getTimeInMillis())));
//            map.put("createUser", mongoTemplate.count(query, HBUser.class));
//        }
//        {
//            query = new Query();
//            cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.set(Calendar.SECOND, 0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.MILLISECOND, 0);
//            query.addCriteria(Criteria.where("lastLoginDate")
//                                      .lt(new Date())
//                                      .gt(new Date(cal.getTimeInMillis())));
//            map.put("loginUser", mongoTemplate.count(query, HBUser.class));
//        }
//        {
//            query = new Query();
//            query.addCriteria(Criteria.where("createTime")
//                                      .lt(new Date())
//                                      .gt(new Date(cal.getTimeInMillis())));
//            long answerCount = mongoTemplate.count(query, HBAnswerHistory.class);
//            map.put("answer", answerCount);
//        }
//        {
//            map.put("visit", visitStatusService.getTodayStatus().getCount());
//        }
//        return map;
//    }
//
//    public JSONObject getBelongToMe() {
//        JSONObject rsMap = new JSONObject();
//        { // 检查有多少业务仍旧未处理
//            Query query = new Query(Criteria.where("statecurrent")
//                                            .lt(HBWorkflowStateEnum.STATE_HAS_SOLVED.getIndex()));
//            Long count = mongoTemplate.count(query, WorkflowState.class);
//            rsMap.put("unfinishedWorkflow", count);
//        }
//        { // 检查有多少业务已得到处理
//            Query query = new Query(Criteria.where("statecurrent")
//                                            .gte(HBWorkflowStateEnum.STATE_HAS_SOLVED.getIndex()));
//            Long count = mongoTemplate.count(query, WorkflowState.class);
//            rsMap.put("finishedWorkflow", count);
//        }
//        { // 检查有多少问题还未处理
//            Criteria criteria = Criteria.where("statecurrent")
//                                        .lt(HBQuestionStateEnum.STATE_HAS_PICK_ANSWER.getIndex());
//            Query query = new Query(criteria);
//            Long count = mongoTemplate.count(query, HBQuestion.class);
//            rsMap.put("unfinishedQuestion", count);
//        }
//        { // 检查有多少问题已得到处理
//            Criteria criteria = Criteria.where("statecurrent")
//                                        .gte(HBQuestionStateEnum.STATE_HAS_PICK_ANSWER.getIndex());
//            Query query = new Query(criteria);
//            Long count = mongoTemplate.count(query, HBQuestion.class);
//            rsMap.put("finishedQuestion", count);
//        }
//        return rsMap;
//    }
//
//    /**
//     * 发送系统信息邮件，每天17:05时生成一次
//     */
//    @Scheduled(cron = "0 5 17 * * *")
//    public void sendHtmlMail() {
//        logger.info("发送系统信息邮件");
//        logger.info("runOnDev:" + sysConfService.getRunOnDev());
//        if (!sysConfService.getRunOnDev()) { // 线上运行
//            MimeMessage message = null;
//            try {
//                message = mailSender.createMimeMessage();
//                MimeMessageHelper helper = new MimeMessageHelper(message, true);
//                StringBuffer sb = new StringBuffer();
//                JSONObject jsonToMe = getBelongToMe();// 获取业务信息
//                Map<String, Object> mapToMe = jsonToMe;
//                Map<String, Object> mapCount = getSystemInfoCount();// 获取系统信息
//                helper.setFrom(sysConfService.getSpringMailUsername());
//                helper.setTo("web_tech@helper12366.com");
//                // helper.setTo(Sender);
//                helper.setSubject("测试发送1：发送Html内容-》线上系统状况");
//                sb.append("<h1>" + TimerService.now_to_day + "线上系统运行状况</h1>")
//                  .append("<table border=\"1\">\n" + "  <tr>\n" + "    <th>用户总数</th>\n"
//                          + "    <th>最近七天新增人数</th>\n" + "    <th>今日登陆人数</th>\n"
//                          + "    <th>今日服务调用量</th>\n" + "    <th>还未处理业务</th>\n"
//                          + "    <th>已处理业务</th>\n" + "    <th>还未处理问题</th>\n" + "    <th>处理问题</th>\n"
//                          + "  </tr>\n" + "  <tr>\n" + "    <td>" + mapCount.get("sumUser")
//                          + "</td>\n" + "    <td>" + mapCount.get("createUser") + "</td>\n"
//                          + "    <td>" + mapCount.get("loginUser") + "</td>\n" + "    <td>"
//                          + mapCount.get("visit") + "</td>\n" + "    <td>"
//                          + mapToMe.get("unfinishedWorkflow") + "</td>\n" + "    <td>"
//                          + mapToMe.get("finishedWorkflow") + "</td>\n" + "    <td>"
//                          + mapToMe.get("unfinishedQuestion") + "</td>\n" + "    <td>"
//                          + mapToMe.get("finishedQuestion") + "</td>\n" + "  </tr>\n" + "</table>");
//                helper.setText(sb.toString(), true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mailSender.send(message);
//            logger.info("发送邮件成功");
//        }
//    }
//
//    /**
//     * 生成网站地图方法
//     */
//    public String CreateSitemap(String path) {
//        String filename = path + "sitemapIndex.xml";
//        File file = new File(filename);
//        if (file.exists()
//                && (System.currentTimeMillis() - file.lastModified()) / (1000 * 60) > 1440) {// 文件创建时间超过24小时重新生成
//            try {
//                WebSitemakgenerator sitemakgenerator = WebSitemakgenerator.builder("http://www.helper12366.com",
//                                                                                   new File(path))
//                                                                          .gzip(false)
//                                                                          .build();
//                // 主页url写入
//                WebSitemapUrl homeurl = gethomesitemapUrl("http://www.helper12366.com");
//                sitemakgenerator.addUrl(homeurl);
//                // 法规url写入
//                List<HBLawSitemap> listlaw = mongoTemplate.find(new Query().addCriteria(Criteria.where("state")
//                                                                                                .is(true)),
//                                                                HBLawSitemap.class);
//                for (HBLawSitemap str : listlaw) {
//                    String name = str.getId();
//                    WebSitemapUrl lawurl = getlawsitemapUrl(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                // 文章政策解读url写入
//                Query querpolicy = new Query(Criteria.where("categorys").regex("政策解读"));
//                querpolicy.addCriteria(Criteria.where("state").is(2));
//                List<HBArticleSitemap> listpolicy = mongoTemplate.find(querpolicy,
//                                                                       HBArticleSitemap.class);
//                for (HBArticleSitemap str : listpolicy) {
//                    String name = str.getId(); // sitemapUrl和文件名
//                    WebSitemapUrl lawurl = getpolicysitemapUrl(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                // 文章热点专题url写入
//                Query querhot = new Query(Criteria.where("categorys").regex("热点专题"));
//                querhot.addCriteria(Criteria.where("state").is(2));
//                List<HBArticleSitemap> listarticlehot = mongoTemplate.find(querhot,
//                                                                           HBArticleSitemap.class);
//                for (HBArticleSitemap str : listarticlehot) {
//                    String name = str.getId(); // sitemapUrl和文件名
//                    WebSitemapUrl lawurl = getHotsitemapUrl(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                // 文章首页新闻url写入
//                Query querNew = new Query(Criteria.where("categorys").regex("首页新闻"));
//                querNew.addCriteria(Criteria.where("state").is(2));
//                List<HBArticleSitemap> listarticleNew = mongoTemplate.find(querNew,
//                                                                           HBArticleSitemap.class);
//                for (HBArticleSitemap str : listarticleNew) {
//                    String name = str.getId(); // sitemapUrl和文件名
//                    WebSitemapUrl lawurl = getHotsitemapNew(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                // 文章名家税谈url写入
//                Query querblog = new Query(Criteria.where("categorys").regex("名家税谈"));
//                querblog.addCriteria(Criteria.where("state").is(2));
//                List<HBArticleSitemap> listarticleblog = mongoTemplate.find(querblog,
//                                                                            HBArticleSitemap.class);
//                for (HBArticleSitemap str : listarticleblog) {
//                    String name = str.getId(); // sitemapUrl和文件名
//                    WebSitemapUrl lawurl = getBlogsitemapUrl(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                // 问答url写入
//                Query query = new Query();
//                query.limit(1100);
//                query.with(Sort.by(new Order(Direction.DESC, "visitNum")));
//                List<HBQuestionStandardBasic> listquestion = mongoTemplate.find(query,
//                                                                                HBQuestionStandardBasic.class,
//                                                                                "hb_question_standards");
//                for (HBQuestionStandardBasic str : listquestion.subList(0, 1000)) {// 前1000条
//                    String name = str.getId(); // sitemapUrl和文件名
//                    WebSitemapUrl lawurl = getquestionsitemapUrl(name);
//                    sitemakgenerator.addUrl(lawurl);
//                }
//                sitemakgenerator.write();
//                indexSitemap(filename);// 生成sitemap索引文件
//                return "网站地图创建完成";
//            } catch (Exception e) {
//                e.printStackTrace();
//                return "网站地图创建过程出现异常";
//            }
//        }
//        return "网站地图未到更新时间";
//    }
//
//    /**
//     * 主页url 权重1.0 页面内容更新周期一天
//     */
//    public WebSitemapUrl gethomesitemapUrl(String str) {
//        String policyurl = str;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(1.0)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 政策解读url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getpolicysitemapUrl(String filenme) {
//        // String policyurl = "http://www.helper12366.com/craw/" + filenme;
//        String policyurl = "http://www.helper12366.com/detail/policy?id=" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 文章名家税谈url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getBlogsitemapUrl(String filenme) {
//        String policyurl = "http://www.helper12366.com/detail/blog?id=" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 文章热点专题url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getHotsitemapUrl(String filenme) {
//        String policyurl = "http://www.helper12366.com/detail/article?id=" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 首页新闻url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getHotsitemapNew(String filenme) {
//        String policyurl = "http://www.helper12366.com/news/" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 法规url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getlawsitemapUrl(String filenme) {
//        String policyurl = "http://www.helper12366.com/detail/law?id=" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 问答url 权重0.8 页面内容更新周期一天
//     */
//    public WebSitemapUrl getquestionsitemapUrl(String filenme) {
//        String policyurl = "http://www.helper12366.com/taxAnswer/detail?question=" + filenme;
//        try {
//            WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(policyurl).lastMod(TimerService.now_to_day)
//                                                                           .priority(0.8)
//                                                                           .changeFreq(ChangeFreq.DAILY)
//                                                                           .build();
//            return sitemapUrl;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * sitemap索引文件写入
//     * @param path
//     */
//    public void indexSitemap(String path) {
//        String nowtime = TimerService.now_to_day;
//        try {
//            PrintStream printStream = new PrintStream(new FileOutputStream(path));// 不存在不会创建
//            StringBuilder sb = new StringBuilder();
//            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                    + "<sitemapindex  xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" >\n"
//                    + " <sitemap>\n" + "      <loc>http://www.helper12366.com/sitemap.xml</loc>\n"
//                    + "      <lastmod>" + nowtime + "</lastmod>\n" + "   </sitemap>\n"
//                    + "   <sitemap>\n"
//                    + "      <loc>http://www.helper12366.com/sitemap1.xml</loc>\n"
//                    + "      <lastmod>" + nowtime + "</lastmod>\n" + "   </sitemap>\n"
//                    + "   <sitemap>\n"
//                    + "      <loc>http://www.helper12366.com/sitemap2.xml</loc>\n"
//                    + "      <lastmod>" + nowtime + "</lastmod>\n" + "   </sitemap>\n"
//                    + "   <sitemap>\n"
//                    + "      <loc>http://www.helper12366.com/sitemap3.xml</loc>\n"
//                    + "      <lastmod>" + nowtime + "</lastmod>\n" + "   </sitemap>\n"
//                    + "   <sitemap>\n"
//                    + "      <loc>http://www.helper12366.com/sitemap4.xml</loc>\n"
//                    + "      <lastmod>" + nowtime + "</lastmod>\n" + "   </sitemap>\n"
//                    + " </sitemapindex>");
//            printStream.println(sb.toString());
//            printStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}