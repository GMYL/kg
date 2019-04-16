package hb.kg.msg.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import hb.kg.common.service.BaseService;

@Service
public class SmsAliService extends BaseService {
    private IAcsClient acsClient;

    @PostConstruct
    public void init() {
        // 调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        try {
            // 初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                                                               sysConfService.getSmsAccessKeyId(),
                                                               sysConfService.getSmsAccessKeySecret());
            DefaultProfile.addEndpoint("cn-hangzhou",
                                       "cn-hangzhou",
                                       sysConfService.getSmsProduct(),
                                       sysConfService.getSmsDomain());
            acsClient = new DefaultAcsClient(profile);
            logger.info("短信客户端初始化成功");
        } catch (Exception e) {
            logger.error("短信客户端初始化异常", e);
        }
    }

    public SendSmsResponse sendRegisterCode(String phoneNum,
                                            String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_125005066");
    }

    public SendSmsResponse sendActivaCode(String phoneNum,
                                          String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_150737703");
    }

    public SendSmsResponse sendLoginErrorCode(String phoneNum,
                                              String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_125005067");
    }

    public SendSmsResponse sendLoginSmsCode(String phoneNum,
                                            String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_125005068");
    }

    public SendSmsResponse sendChangePwdCode(String phoneNum,
                                             String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_125005065");
    }

    public SendSmsResponse sendCommonVerifyCode(String phoneNum,
                                                String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_139865131");
    }

    public SendSmsResponse sendVerifyCode(String phoneNum,
                                          String verifyCode,
                                          String templateCode) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName("花瓣儿网");
        request.setTemplateCode(templateCode);
        request.setTemplateParam("{\"code\":\"" + verifyCode + "\"}");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum + ":" + verifyCode, e);
        }
        return sendSmsResponse;
    }

    public SendSmsResponse sendWenDa(String phoneNum,
                                     String name,
                                     String title) {
        // 组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        // 必填:待发送手机号
        request.setPhoneNumbers(phoneNum);
        // 必填:短信签名-可在短信控制台中找到
        request.setSignName("花瓣儿网");
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode("SMS_123739928");
        // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        request.setTemplateParam("{\"name\":\"" + name + "\", \"title\":\"" + title + "\"}");
        // hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum + ":" + name, e);
        }
        return sendSmsResponse;
    }

    public SendSmsResponse sendWorkflowChanged(String phoneNum,
                                               String name,
                                               String title,
                                               String content) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName("花瓣儿网");
        request.setTemplateCode("SMS_140110951");
        request.setTemplateParam("{\"name\":\"" + name + "\", \"title\":\"" + title
                + "\", \"content\":\"" + content + "\"}");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum + ":" + name, e);
        }
        return sendSmsResponse;
    }

    public SendSmsResponse sendWorkflowFinished(String phoneNum,
                                                String name,
                                                String title) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName("花瓣儿网");
        request.setTemplateCode("SMS_139239174");
        request.setTemplateParam("{\"name\":\"" + name + "\", \"title\":\"" + title + "\"}");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum + ":" + name, e);
        }
        return sendSmsResponse;
    }

    public SendSmsResponse sendCircleCode(String phoneNum,
                                          String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_154590169");
    }

    public SendSmsResponse sendCustomCode(String phoneNum,
                                          String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_154590140");
    }

    public SendSmsResponse sendCooperationCode(String phoneNum,
                                               String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_154590138");
    }

    public SendSmsResponse sendRecruitingCode(String phoneNum,
                                              String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_154590135");
    }

    public SendSmsResponse sendCultivateCode(String phoneNum,
                                             String verifyCode) {
        return sendVerifyCode(phoneNum, verifyCode, "SMS_154585151");
    }

    public SendSmsResponse sendCooperationMessage(String phoneNum,
                                                  String message) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName("花瓣儿网");
        request.setTemplateCode("SMS_154586538");
        request.setTemplateParam("{\"smg\":\"" + message + "\"}");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum + ":" + message, e);
        }
        return sendSmsResponse;
    }

    public SendSmsResponse sendSpreadMessage(String phoneNum) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName("花瓣儿网");
        request.setTemplateCode("SMS_154586538");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (Exception e) {
            logger.error("短信发送失败" + phoneNum, e);
        }
        return sendSmsResponse;
    }
}
