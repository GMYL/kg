package hb.kg.common.service;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import hb.kg.common.util.set.HBStringUtil;
import lombok.Getter;

/**
 * 所有配置文件内的程序用到的配置项都从这里走，这样整个系统的调用是干净的，可以很快知道哪个配置项有没有用到
 * @INFO 使用PropertiesUtil线下生成
 * @INFO 将来会改为这里是默认值，如果需要改动再配置到property文件中的方式
 */
@Getter
@Service
public class SysConfService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Environment environment;

    /**
     * 从这里对所有配置项进行填入 ;
     * @INFO 1.获取本类的所有声明的字段
     * @INFO 2.大小写转换并加上.与properties文件字段保持一致
     * @INFO 3.如果properties中存在的字段，覆盖本类字段
     */
    @PostConstruct
    public void init() {
        Field[] fields = SysConfService.class.getDeclaredFields();
        boolean goon = false;
        for (Field field : fields) {
            if (field.getName().equals("runOnDev")) {
                // 从这个配置开示
                goon = true;
            }
            if (goon) {
                try {
                    char[] keyChars = field.getName().toCharArray();
                    StringBuilder sb = new StringBuilder();
                    for (char c : keyChars) {
                        if (Character.isUpperCase(c)) {
                            sb.append(".").append(Character.toLowerCase(c));
                        } else {
                            sb.append(c);
                        }
                    }
                    String propertyValue = environment.getProperty(sb.toString());
                    if (HBStringUtil.isNotBlank(propertyValue)) {
                        field.setAccessible(true);
                        switch (field.getType().getName()) {
                        case "java.lang.Boolean":
                            field.set(this, Boolean.parseBoolean(propertyValue));
                            break;
                        case "java.lang.Integer":
                            field.set(this, Integer.parseInt(propertyValue));
                            break;
                        case "java.lang.Long":
                            field.set(this, Long.parseLong(propertyValue));
                            break;
                        case "java.lang.String":
                            field.set(this, propertyValue);
                        default:
                            break;
                        }
                    }
                    logger.info("配置项【" + sb.toString() + "】=" + field.get(this));
                } catch (Exception e) {
                    logger.error("配置项【" + field.getName() + "】读取出错", e);
                }
            }
        }
    }

    // 从这里开始都是配置文件内的变量，如果需要赋值默认值直接后面设置就行
    private Boolean runOnDev = true;
    private String springProfilesActive;
    private String serverHost = "localhost";
    private Integer serverPort = 18090;
    private Boolean springDevtoolsRestartEnabled;
    // 各种开关，默认都是关闭的，如果需要开启在配置文件中进行开启
    private Boolean switchOnlineRedis = false; // 是否启用redis缓存
    private Boolean switchOnlineEs = false; // 是否启用es搜索引擎
    private Boolean switchOnlineQueue = false; // 是否启用队列服务
    private Boolean switchOnlineBackup = false; // 是否启用自动备份
    private Boolean switchOnlineSms = false; // 是否启用短信服务
    private Boolean switchOnlineReport = false; // 是否启用生成报告服务
    private Boolean swaggerEnable = true; // 默认swagger打开，上线的时候请关闭
    // mongo的配置，spring开头的配置必须在配置文件中写
    private String springDataMongodbHost = "192.168.1.70:8091";
    private Integer springDataMongodbPort;
    private String springDataMongodbDatabase = "backend_kg";
    private String springDataMongodbAuthenticationDatabase = "backend_kg";
    private String springDataMongodbUsername = "backend";
    private String springDataMongodbPassword = "huaban2019";
    private String mongoHomeLoc = "/data/huaban/libs/mongo";
    // redis的配置，spring开头的配置必须在配置文件中写
    private String springRedisDatabase;
    private String springRedisHost;
    private Integer springRedisPort;
    private String springRedisPassword;
    private Long springRedisTimeout;
    private String springRedisKeyprefix;
    private String esClusterName;
    private String esClusterMachines;
    private Integer esFreshStarttime;
    private Boolean esSniffOpen;
    private Integer nettyTcpPort;
    private Integer nettyBossThreadCount;
    private Integer nettyWorkerThreadCount;
    private String nettySoKeepalive;
    private Integer nettySoBacklog;
    private String nettyTestToken;
    private String apiPrefixName = "kg";
    private String apiVersion = "v1";
    private String springApplicationName = "kg";
    private String ossAccessId;
    private String ossAccessKey;
    private String ossEndPoint;
    private String ossBucketName;
    private String ossBucketArticlemediaName;
    // 特殊名称
    private String anonymouslyModuleName = "no_auth"; // 系统模块中匿名的模块
    // 队列相关
    private String scheduledThreadPoolSize;
    private Integer queueJobSize;
    private Integer queueSlaveSize;
    private Integer queueExitWaitSecond;
    private Boolean threadFactoryMakeDaemon;
    private String loggingLevelRoot;
    private String loggingLevelHb;
    private String loggingLevelSpringfoxDocumentationSpringWeb;
    private String loggingLevelHbBkUserAuthFilterMyFilterSecurityInterceptor;
    private String loggingLevelOrgSpringframeworkWebServletMvcMethodAnnotationRequestMappingHandlerMapping;
    private String loggingLevelOrgSpringframeworkDataMongodbCoreMappingBasicMongoPersistentProperty;
    private String loggingPatternConsole;
    private String loggingPatternFile;
    private Boolean useSystemLog = false; // 是否使用系统日志，主动记录log到mongo中的时候用到
    private String sysLogDir;
    private String robotPassword;
    private String springMailHost;
    private String springMailUsername;
    private String springMailPassword;
    private String springMailPort;
    private String springMailPropertiesMailSmtpSocketFactoryClass;
    private String springMailPropertiesMailSmtpAuth;
    private String msgMailHost;
    private String msgMailUser;
    private String msgMailPasswd;
    // 上传下载相关
    private long mongoTempCleanPeriod = 24l * 60 * 60; // mongo中的临时表的数据清理时间，默认24小时，对应hb_temp
    private String downloadLoc = "/data/images/download/";
    private String tempLoc = "/data/images/temp/";
    private String downloadUrlPrefix = "http://localhost/hb/static/download/";
    private String sitemapLoc;
    private String dictLoc;
    // 账户配置
    private String tokenHeader = "hbtoken"; // 秘钥在用户浏览器的header中保留的头数据
    private String jwtSecret;
    private Long sessionExpiration = 10800000l; // 用户session过期时间（默认3小时）
    private Long tokenExpiration = 172800000l; // 登陆秘钥过期时间（默认2天）
    private String tokenPrefix = "hbtoken_"; // 秘钥的前缀
    private String tokenSaltKey = "huaban_means_family";
    private String tokenFstAesSalt = "e6ade95615b3e13374f4a05a591c21c1"; // 秘钥第一段加秘的key
    private String tokenSndAesSalt = "7ff2cf0a71bba63a7fe59342b523daf4"; // 秘钥第二段加密的key
    // 备份文件配置
    private String backupLoc = "/data/backup"; // 备份文件保存路径
    private Integer backupCleanTtl = 10; // 数据库备份文件过期时间（单位：天）
    // 短信
    private String smsProduct = "Dysmsapi"; // 产品名称:云通信短信API产品,开发者无需替换
    private String smsDomain = "dysmsapi.aliyuncs.com"; // 产品域名,开发者无需替换
    private String smsAccessKeyId = "LTAIvbG92LjLYTHs"; // 开发者自己的AK
    private String smsAccessKeySecret = "afsyUfLObq12LHRyik5bwHRqh7ZaCu";
    // 爬虫相关
    private Boolean switchOnlineCrawler = false; // 是否启用爬虫
    private String crawlerPushkey; // 文章推送秘钥
    private String crawlerPushUrl; // 文章线上推送地址
}
