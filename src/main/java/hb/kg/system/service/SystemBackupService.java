package hb.kg.system.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import hb.kg.common.service.BaseService;
import hb.kg.common.service.TimerService;
import hb.kg.common.util.file.HBRunCommand;
import hb.kg.system.bean.http.HBBackUpFile;

/**
 * 系统备份服务
 */
@Service
public class SystemBackupService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(SystemBackupService.class);
    @Autowired
    private SystemRunningLogService systemRunningLogService;
    private AtomicBoolean onGenerate = new AtomicBoolean(false);

    /**
     * 每天3:00定时备份数据库文件
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleBackUpMongoDB() {
        if (sysConfService.getSwitchOnlineBackup()) {
            logger.info("开始备份数据库文件");
            String result = doBackUpMongoDB();
            logger.info("数据库文件备份完成:" + result);
            systemRunningLogService.addSystemRunningLog("backup", "生成当前系统的备份文件：" + result);
            logger.info("开始清理过期数据库文件");
            List<String> rmList = removeFileByTTL(24l * 3600 * 1000
                    * sysConfService.getBackupCleanTtl());
            logger.info("成功清理以下数据库备份文件:" + rmList);
            systemRunningLogService.addSystemRunningLog("backup", "成功清理以下数据库备份文件:" + rmList);
        }
    }

    /**
     * 立刻将mongo数据库文件备份到本地，每天凌晨3点执行
     */
    public String doBackUpMongoDB() {
        if (!onGenerate.get()) {
            try {
                onGenerate.set(true);
                File bkdir = new File(sysConfService.getBackupLoc() + "/"
                        + TimerService.now_to_day);
                if (!bkdir.exists()) {
                    bkdir.mkdirs();
                } else {
                    onGenerate.set(false);
                    return "今日的数据库文件已经生成，请不要重复生成";
                }
                StringBuffer sb = new StringBuffer();
                sb.append(sysConfService.getMongoHomeLoc()).append("/bin/mongodump ");
                sb.append(" -u ").append(sysConfService.getSpringDataMongodbUsername());
                sb.append(" -p ").append(sysConfService.getSpringDataMongodbPassword());
                sb.append(" -h ").append(sysConfService.getSpringDataMongodbHost());
                sb.append(" --authenticationDatabase ")
                  .append(sysConfService.getSpringDataMongodbAuthenticationDatabase());
                sb.append(" -d ").append(sysConfService.getSpringDataMongodbDatabase());
                sb.append(" -o ").append(bkdir.getAbsolutePath());
                logger.info("执行命令：" + sb.toString());
                HBRunCommand.runCmdForBoolean(sb.toString());
                onGenerate.set(false);
                return bkdir.getAbsolutePath();
            } catch (Exception e) {
                onGenerate.set(false);
                logger.error("生成备份文件失败", e);
                return "生成失败";
            }
        } else {
            return "备份文件正在生成，请不要重复点击";
        }
    }

    /**
     * 恢复mongo数据库，注意恢复的时候会删掉每一个被恢复的collection
     */
    public String reductionMongoDB(String day) {
        File bkdir = new File(sysConfService.getBackupLoc() + "/" + day);
        if (!bkdir.exists() && StringUtils.isNotBlank(day)) {
            return "数据库文件不存在";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(sysConfService.getBackupLoc()).append("/bin/mongorestore ");
        sb.append(" -u ").append(sysConfService.getSpringDataMongodbUsername());
        sb.append(" -p ").append(sysConfService.getSpringDataMongodbPassword());
        sb.append(" -h ").append(sysConfService.getSpringDataMongodbHost());
        sb.append(" --authenticationDatabase ")
          .append(sysConfService.getSpringDataMongodbAuthenticationDatabase());
        sb.append(" --drop ");
        sb.append(" -d ").append(sysConfService.getSpringDataMongodbDatabase());
        sb.append(" ")
          .append(bkdir.getAbsolutePath())
          .append("/")
          .append(sysConfService.getSpringDataMongodbDatabase());
        HBRunCommand.runCmdForBoolean(sb.toString());
        return bkdir.getAbsolutePath();
    }

    /**
     * 显示所有备份文件
     */
    public List<HBBackUpFile> listAllFile() {
        List<HBBackUpFile> rsList = new ArrayList<>();
        File backupDir = new File(sysConfService.getBackupLoc());
        if (StringUtils.isNotEmpty(sysConfService.getBackupLoc())) {
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            if (!backupDir.isDirectory()) {
                return null;
            }
            File[] allBkFiles = backupDir.listFiles();
            if (allBkFiles != null) {
                for (File file : allBkFiles) {
                    HBBackUpFile hbbuf = new HBBackUpFile();
                    hbbuf.setFilename(file.getName());
                    hbbuf.setLastModifyTime(file.lastModified());
                    hbbuf.setLoc(file.getParent());
                    hbbuf.setSize(HBRunCommand.getDirSize(file.getAbsolutePath()));
                    rsList.add(hbbuf);
                }
            }
        }
        return rsList;
    }

    /**
     * 根据ttl删除所有过期文件
     */
    public List<String> removeFileByTTL(long ttl) {
        List<String> rsList = new ArrayList<>();
        File backupDir = new File(sysConfService.getBackupLoc());
        if (ttl > 0 && StringUtils.isNotEmpty(sysConfService.getBackupLoc()) && backupDir.exists()
                && backupDir.isDirectory()) {
            File[] allBkFiles = backupDir.listFiles();
            if (allBkFiles != null) {
                long nowTime = System.currentTimeMillis();
                for (File file : allBkFiles) {
                    if (nowTime - ttl > file.lastModified()) {
                        if (HBRunCommand.rm(file.getAbsolutePath())) {
                            rsList.add(file.getName());
                        } else {
                            logger.error("删除备份文件" + file.getName() + "失败");
                        }
                    }
                }
            }
        }
        return rsList;
    }

    /**
     * 直接删除备份文件
     */
    public String removeBackupFile(String filename) {
        File backupFile = new File(sysConfService.getBackupLoc() + "/" + filename);
        if (StringUtils.isNotEmpty(filename) && backupFile.exists() && backupFile.isDirectory()) {
            if (HBRunCommand.rm(backupFile.getAbsolutePath())) {
                return "删除" + backupFile.getAbsolutePath() + "成功";
            }
        }
        return "删除" + backupFile.getAbsolutePath() + "失败";
    }
}
