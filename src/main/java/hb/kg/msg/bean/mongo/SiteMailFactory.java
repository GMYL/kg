package hb.kg.msg.bean.mongo;

import org.apache.commons.lang3.StringUtils;

import hb.kg.msg.bean.http.HBMultiSiteMail;

public class SiteMailFactory {
    /**
     * 检查发来的一个邮件对象是否正常
     */
    public static boolean checkSitemailValid(HBSiteMail mail) {
        if (mail == null || mail.getTo() == null || StringUtils.isEmpty(mail.getTo().getId())
                || mail.getFrom() == null || StringUtils.isEmpty(mail.getFrom().getId())
                || mail.getContent() == null || StringUtils.isEmpty(mail.getContent())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查发来的一个群发邮件对象是否正常
     */
    public static boolean checkMultiSitemailValid(HBMultiSiteMail mail) {
        if (mail == null || mail.getToIds() == null || mail.getToIds().size() == 0
                || mail.getFromId() == null || StringUtils.isEmpty(mail.getFromId())
                || mail.getContent() == null || StringUtils.isEmpty(mail.getContent())) {
            return false;
        } else {
            return true;
        }
    }

    public static HBSiteMail generateSiteMail(HBSiteMail mail) {
        mail = mail == null ? new HBSiteMail() : mail;
        mail.prepareHBBean();
        return mail;
    }

    public static HBSiteMail generateSiteMail() {
        return generateSiteMail(new HBSiteMail());
    }
}
