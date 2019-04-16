package hb.kg.msg.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.springframework.stereotype.Service;

import hb.kg.common.service.BaseService;

/**
 * 邮件发送服务，调用方式见main函数
 */
@Service
public class EmailService extends BaseService {

    public boolean sendEmail(String to,
                             String subject,
                             String content,
                             String attachment) {
        List<String> tos = new LinkedList<>();
        tos.add(to);
        return sendEmail(tos, subject, content, attachment);
    }

    public boolean sendEmail(List<String> to,
                             String title,
                             String content,
                             String attachment) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", sysConfService.getMsgMailHost());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.user", sysConfService.getMsgMailUser());
        properties.put("mail.smtp.password", sysConfService.getMsgMailPasswd());
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sysConfService.getMsgMailUser(), sysConfService.getMsgMailPasswd());
            }
        };
        Session session = Session.getInstance(properties, auth);
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(sysConfService.getMsgMailUser()));
            Address[] toArray = new Address[to.size()];
            for (int i = 0; i < to.size(); i++) {
                toArray[i] = InternetAddress.parse(to.get(i))[0];
            }
            msg.setRecipients(Message.RecipientType.TO, toArray);
            // msg.setSubject(subject, "GBK");
            msg.setSubject(MimeUtility.encodeText(title, MimeUtility.mimeCharset("UTF-8"), null));
            // msg.setSubject("=?GB2312?B?"+subject+"?=");
            msg.setSentDate(new Date(System.currentTimeMillis()));
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("<meta http-equiv=Content-Type content=text/html; charset=UTF-8>"
                    + content, "text/html;charset=UTF-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            if (attachment != null && attachment.trim().length() > 0
                    && !attachment.trim().equalsIgnoreCase("0")) {
                try {
                    BodyPart part = new MimeBodyPart();
                    FileDataSource fileds = new FileDataSource(attachment);
                    part.setDataHandler(new DataHandler(fileds));
                    part.setFileName(fileds.getName());
                    multipart.addBodyPart(part);
                } catch (Exception e) {
                    logger.error("设置附件", e);
                    return false;
                }
            }
            msg.setContent(multipart);
            Transport transport = session.getTransport("smtp");
            transport.connect((String) properties.get("mail.smtp.host"),
                              sysConfService.getMsgMailUser(),
                              sysConfService.getMsgMailPasswd());
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
            return true;
        } catch (Exception e) {
            logger.error("send email [" + title + "] to [" + to.toString() + "] failed. ", e);
        }
        return false;
    }

    public static String formatMail(String info) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><head></head><body style=\"font-family:Consolas,\'Microsoft YaHei\';\">");
        sb.append(info);
        sb.append("</body></html>");
        return sb.toString();
    }
}
