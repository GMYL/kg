package hb.kg.msg.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.msg.bean.http.HBMultiSiteMail;
import hb.kg.msg.bean.mongo.HBSiteMail;
import hb.kg.msg.bean.mongo.SiteMailFactory;
import hb.kg.msg.dao.SiteMailDao;
import hb.kg.user.bean.http.HBUserBasic;

/**
 * 用户站内信收发，因为过去通过redis实现了交互且效果也并不理想，所以先都按照数据库的交互实现，今后用session来实现用户心跳的接收
 */
@Service
public class SiteMailService extends BaseCRUDService<HBSiteMail> {
    private static final Logger logger = LoggerFactory.getLogger(SiteMailService.class);
    @Resource
    private SiteMailDao siteMailDao;

    public BaseDao<HBSiteMail> dao() {
        return siteMailDao;
    }

    /**
     * 发送站内信
     */
    public boolean sendInternMail(String from,
                                  String to,
                                  String title,
                                  String content) {
        // 首先把站内信先保存到数据库中
        if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to) || StringUtils.isEmpty(content)) {
            logger.error("user[" + from + "] send to[" + to
                    + "] internal mail EMPTY error!, content={" + content + "}");
            return false;
        }
        HBSiteMail interMail = SiteMailFactory.generateSiteMail();
        interMail.setFrom(new HBUserBasic(from));
        interMail.setTo(new HBUserBasic(to));
        interMail.setTitle(title);
        interMail.setContent(content);
        return sendInternMail(interMail);
    }

    public boolean sendInternMail(HBSiteMail mail) {
        // 首先把站内信先保存到数据库中
        if (!SiteMailFactory.checkSitemailValid(mail)) {
            return false;
        }
        mail.prepareHBBean();
        siteMailDao.insert(mail);
        return true;
    }

    public boolean sendMultiInternMail(HBMultiSiteMail multiMails) {
        // 首先把站内信先保存到数据库中
        if (!SiteMailFactory.checkMultiSitemailValid(multiMails)) {
            return false;
        }
        List<HBSiteMail> siteMails = multiMails.toMailList();
        if (siteMails == null || siteMails.size() < 0) {
            return false;
        }
        for (HBSiteMail sm : siteMails) {
            siteMailDao.insert(sm);
        }
        return true;
    }

    /**
     * 检查mongo是否有未读的站内信，如果有，则加入redis
     */
    public Map<String, String> checkMongoInternalMail(String userid) {
        Map<String, Object> statesMap = new HashMap<>();
        statesMap.put("toId", userid);
        statesMap.put("isRead", false);
        Collection<HBSiteMail> mails = siteMailDao.findAll(statesMap);
        if (mails != null && mails.size() > 0) {
            HashMap<String, String> mailMap = new HashMap<>();
            for (HBSiteMail mail : mails) {
                mailMap.put(mail.getId(),
                            mail.getContent().length() > 100 ? mail.getContent().substring(0, 100)
                                    : mail.getContent());
            }
            return mailMap;
        }
        return null;
    }

    /**
     * 从mongo中读取站内信全文
     */
    public HBSiteMail getMailFullMsg(String mailid) {
        return siteMailDao.findOne(mailid);
    }

    public boolean fakeDelALot(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            Query query = new Query(Criteria.where("id").in(ids));
            Update update = new Update();
            update.set("fromValid", false);
            mongoTemplate.updateMulti(query, update, HBSiteMail.class);
            return true;
        } else {
            return false;
        }
    }
}
