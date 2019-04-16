package hb.kg.user.controller.f;

import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.mongodb.client.result.UpdateResult;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseBeanCRUDController;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.util.id.ValidateUtil;
import hb.kg.msg.service.SmsAliService;
import hb.kg.user.bean.http.HBUserAccount;

/**
 * 后台对各个用户数据的管理
 */
@RestController
@RequestMapping(value = "/${api.version}/f/useraccount")
public class UserAccountFController extends BaseBeanCRUDController<HBUserAccount> {
    @Autowired
    private SmsAliService smsAliService;
    @Autowired
    private AuthService authService;
    @Resource
    private StringRedisDao stringRedisDao;

    @RequestMapping(value = "changePwdSms", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean changePwdSms(@RequestBody HBUserAccount user) {
        ResponseBean responseBean = getReturn();
        String phoneStr = user.getPhoneNum();
        if (!ValidateUtil.isChinaPhoneNum(phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("用户输入手机号格式错误");
        } else {
            // 判断是否重复发送验证码
            long nowTime = System.currentTimeMillis();
            String lastTimeStr = stringRedisDao.findOne(phoneStr + ".changePwdSmsCodeTime");
            if (StringUtils.isNotEmpty(lastTimeStr)) {
                long lastTime = Long.parseLong(lastTimeStr);
                if (nowTime - lastTime < 60 * 3600) {
                    responseBean.setCode(ApiCode.API_FREQUENCY_TOO_HIGH.getCode());
                    responseBean.setErrMsg("验证码发送太频繁，请等待1分钟以后再发送");
                    return returnBean(responseBean);
                }
            }
            // 发送验证码
            try {
                String smsCode = mainServer.conf().getSwitchOnlineSms()
                        ? "" + new Random().nextInt(999999)
                        : "1234";
                SendSmsResponse smsResponse = smsAliService.sendChangePwdCode(phoneStr, smsCode);
                if ("OK".equals(smsResponse.getCode())) {
                    stringRedisDao.insertOne(phoneStr + ".changePwdSmsCode", smsCode, 5 * 60);
                    stringRedisDao.insertOne(phoneStr + ".changePwdSmsCodeTime",
                                             System.currentTimeMillis() + "",
                                             5 * 60);
                    stringRedisDao.insertOne(phoneStr + ".changePwdPhoneNum", phoneStr);
                } else {
                    responseBean.setErrMsg(smsResponse.getCode());
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                }
            } catch (Exception e) {
                logger.error("发送短信错误：用户手机号[" + phoneStr + "]", e);
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("短信发送失败");
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 验证过验证码后，重置用户密码
     */
    @RequestMapping(value = "changePwd", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean changePwd(@RequestBody HBUserAccount hbUser) {
        ResponseBean responseBean = getReturn();
        String pasword = hbUser.getPassword();
        String phoneStr = hbUser.getPhoneNum();
        if (StringUtils.isEmpty(pasword) || pasword.length() < 6) {
            // 输入的密码格式错误
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("输入密码格式错误");
        } else if (!ValidateUtil.isChinaPhoneNum(phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("手机号码格式错误");
        } else {
            String smsRegCode = stringRedisDao.findOne(phoneStr + ".changePwdSmsCode");
            if (StringUtils.isEmpty(smsRegCode) || StringUtils.isEmpty(hbUser.getSmsCode())
                    || !smsRegCode.equals(hbUser.getSmsCode())) {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("验证码输入错误");
            } else {
                String phoneRedis = stringRedisDao.findOne(phoneStr + ".changePwdPhoneNum");
                if (StringUtils.isEmpty(phoneRedis) || !phoneRedis.equals(phoneStr)) {
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("手机号码验证失败");
                } else {
                    Query query = new Query(Criteria.where("phoneNum").is(phoneStr));
                    Update update = new Update();
                    update.set("password", authService.encodePassword(pasword));
                    UpdateResult wr = mongoTemplate.updateFirst(query,
                                                                update,
                                                                HBUserAccount.class,
                                                                "hb_users");
                    if (wr.getModifiedCount() == 0) {
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setData("用户手机号不正确");
                    }
                }
            }
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "changePhoneSms", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean changePhoneSms(@RequestBody HBUserAccount user) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        String passwd = user.getPassword();
        if (StringUtils.isNotEmpty(userid) && StringUtils.isNotEmpty(passwd)) {
            String phoneStr = user.getPhoneNum();
            if (!ValidateUtil.isChinaPhoneNum(phoneStr)) {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("用户输入手机号格式错误");
            } else {
                // 判断是否重复发送验证码
                long nowTime = System.currentTimeMillis();
                String lastTimeStr = stringRedisDao.findOne(phoneStr + ".changePhoneSmsCodeTime");
                if (StringUtils.isNotEmpty(lastTimeStr)) {
                    long lastTime = Long.parseLong(lastTimeStr);
                    if (nowTime - lastTime < 60 * 3600) {
                        responseBean.setCode(ApiCode.API_FREQUENCY_TOO_HIGH.getCode());
                        responseBean.setErrMsg("验证码发送太频繁，请等待1分钟以后再发送");
                        return returnBean(responseBean);
                    }
                }
                // 校验用户登录密码是否正确
                HBUserAccount oldAccount = baseService.dao().findOne(userid);
                if (oldAccount.getPassword()
                              .equals(authService.encodePassword(user.getPassword()))) {
                    // 校验用户手机号是否在库里存在
                    Query query = new Query(Criteria.where("phoneNum").is(phoneStr));
                    HBUserAccount sameAccount = mongoTemplate.findOne(query,
                                                                      HBUserAccount.class,
                                                                      "hb_users");
                    if (sameAccount == null) {
                        // 发送验证码
                        try {
                            String smsCode = mainServer.conf().getSwitchOnlineSms()
                                    ? "" + new Random().nextInt(999999)
                                    : "1234";
                            SendSmsResponse smsResponse = smsAliService.sendChangePwdCode(phoneStr,
                                                                                          smsCode);
                            if ("OK".equals(smsResponse.getCode())) {
                                stringRedisDao.insertOne(phoneStr + ".changePhoneSmsCode",
                                                         smsCode,
                                                         5 * 60);
                                stringRedisDao.insertOne(phoneStr + ".changePhoneSmsCodeTime",
                                                         System.currentTimeMillis() + "",
                                                         5 * 60);
                                stringRedisDao.insertOne(phoneStr + ".changePhonePhoneNum",
                                                         phoneStr);
                            } else {
                                responseBean.setErrMsg(smsResponse.getCode());
                                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                            }
                        } catch (Exception e) {
                            logger.error("发送短信失败：用户手机号[" + phoneStr + "]", e);
                            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                            responseBean.setErrMsg("短信发送失败");
                        }
                    } else {
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setErrMsg("用户手机号已然存在");
                    }
                }
            }
        } else {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户未登录或密码输入错误");
        }
        return returnBean(responseBean);
    }

    /**
     * 验证过验证码后，重置用户密码
     */
    @RequestMapping(value = "changePhone", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean changePhone(@RequestBody HBUserAccount hbUser) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        String phoneStr = hbUser.getPhoneNum();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户未登录");
        } else if (StringUtils.isEmpty(phoneStr) || !ValidateUtil.isChinaPhoneNum(phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("手机号码格式错误");
        } else {
            String smsRegCode = stringRedisDao.findOne(phoneStr + ".changePhoneSmsCode");
            if (StringUtils.isEmpty(smsRegCode) || StringUtils.isEmpty(hbUser.getSmsCode())
                    || !smsRegCode.equals(hbUser.getSmsCode())) {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("验证码输入错误");
            } else {
                String phoneRedis = stringRedisDao.findOne(phoneStr + ".changePhonePhoneNum");
                if (StringUtils.isEmpty(phoneRedis) || !phoneRedis.equals(phoneStr)) {
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("手机号码验证失败");
                } else {
                    Query query = new Query(Criteria.where("id").is(userid));
                    Update update = new Update();
                    update.set("phoneNum", phoneStr);
                    UpdateResult wr = mongoTemplate.updateFirst(query,
                                                                update,
                                                                HBUserAccount.class,
                                                                "hb_users");
                    if (wr.getModifiedCount() == 0) {
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setData("修改失败");
                    }
                }
            }
        }
        return returnBean(responseBean);
    }

    @Override
    protected String prepareGet(String id,
                                ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isNoneEmpty(userid) && StringUtils.isNotEmpty(id) && userid.equals(id)) {
        } else {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户只能访问自己的数据");
        }
        return super.prepareGet(id, responseBean);
    }

    @Override
    protected HBUserAccount prepareInsert(HBUserAccount user,
                                          ResponseBean responseBean) {
        responseBean.setCode(ApiCode.NO_AUTH.getCode());
        responseBean.setErrMsg("用户没有这个权限");
        return super.prepareInsert(user, responseBean);
    }

    @Override
    protected HBUserAccount prepareUpdate(HBUserAccount user,
                                          ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isNoneEmpty(userid) && StringUtils.isNotEmpty(user.getId())
                && userid.equals(user.getId())) {
        } else {
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
            responseBean.setErrMsg("用户只能修改自己的账户");
        }
        return super.prepareUpdate(user, responseBean);
    }

    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        responseBean.setCode(ApiCode.NO_AUTH.getCode());
        responseBean.setErrMsg("用户不能通过这个接口修改自己的信息");
        return super.prepareRemove(id, responseBean);
    }
}
