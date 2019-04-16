package hb.kg.msg.bean.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hb.kg.msg.bean.mongo.HBSiteMail;
import hb.kg.user.bean.http.HBUserBasic;
import lombok.Data;

@Data
public class HBMultiSiteMail implements Serializable {
    private static final long serialVersionUID = -3104133919276661265L;
    private String fromId; // 发送者Id
    private List<String> toIds; // 接受者Id
    private String title; // 标题
    private String content; // 内容
    private Date sendTime; // 发送时间
    private Boolean toValid; // 对发件人是否可见
    private Boolean fromValid; // 对收件人是否可见

    public List<HBSiteMail> toMailList() {
        List<HBSiteMail> rsMailList = new ArrayList<>();
        if (toIds != null) {
            for (String toid : toIds) {
                HBSiteMail siteMail = new HBSiteMail();
                siteMail.setFrom(new HBUserBasic(fromId));
                siteMail.setTitle(title);
                siteMail.setContent(content);
                siteMail.setToValid(toValid);
                siteMail.setFromValid(fromValid);
                siteMail.setTo(new HBUserBasic(toid));
                siteMail.prepareHBBean();
                rsMailList.add(siteMail);
            }
        }
        return rsMailList;
    }
}
