package hb.kg.common.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import hb.kg.common.bean.http.MarkDownBean;
import hb.kg.common.util.json.JSONObject;

public class HttpRequest {
    public static String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=71c07a276e160c71af25139d7569539d5559140d8ec8d77cf8e4f8c01a421033";

    public static void main(String args[]) throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        // String textMsg = "{ msgtype: \"text\", \"text\": {\"content\": \"我就是我,
        // 是不一样的烟火\"}}";
        // List<>
        JSONObject rsJson = new JSONObject();
        rsJson.put("msgtype", "markdown");
        MarkDownBean mark = new MarkDownBean();
        mark.title = "A0001";
        mark.text = "11111";
        rsJson.put("msgtype", "markdown");
        rsJson.put("markdown", mark);
        System.out.println(rsJson.toString());
        StringEntity se = new StringEntity(rsJson.toString(), "utf-8");
        httppost.setEntity(se);
        HttpResponse response = httpclient.execute(httppost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            System.out.println(result);
        }
    }
}
