package hb.kg.common.controller.auth.f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseController;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.util.id.ValidateUtil;
import hb.kg.msg.service.SmsAliService;
import hb.kg.user.bean.http.HBUserLogin;
import hb.kg.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 之所以要用sessioncode，是为了在用户还未登陆的情况下，对访问用户的唯一性进行判断，尽量这样用，如果用不上可以放到二期
 */
@Api(description = "[F]用户Session")
@Controller
@RequestMapping(value = "/${api.version}/f/auth/")
public class AuthSessionController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(AuthSessionController.class);
    @Autowired
    private SmsAliService smsAliService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Resource
    private StringRedisDao stringRedisDao;

    @ApiOperation(value = "获取 SessionCode", notes = "获取 SessionCode", produces = "application/json")
    @RequestMapping(value = "/getSessionCode")
    @ResponseBody
    public String getSessionCode(HttpServletRequest request) {
        return userService.getSessionCode();
    }

    @ApiOperation(value = "获取 编码 SessionCode", notes = "获取 编码 SessionCode", produces = "application/json")
    @RequestMapping(value = "/getEncodeSessionCode")
    @ResponseBody
    public String getEncodeSessionCode(HttpServletRequest request) {
        return userService.getSessionCode();
    }

    /**
     * 重设密码
     */
    @ApiOperation(value = "重设密码", notes = "重设密码", produces = "application/json")
    @RequestMapping(value = "/changePassword", method = { RequestMethod.POST })
    @ResponseBody
    public ResponseBean changePassword(@ApiParam(value = "用户信息") @RequestBody HBUserLogin user) {
        ResponseBean responseBean = getReturn();
        // 检验手机验证码
        String phoneStr = user.getPhoneNum().trim();
        String smsCode = user.getSmsCode();
        if (ValidateUtil.isChinaPhoneNum(phoneStr)) {
            long nowTime = System.currentTimeMillis();
            String smsCodeSaved = stringRedisDao.findOne(phoneStr + ".smsCode");
            String lastSmsTime = stringRedisDao.findOne(phoneStr + ".lastSmsTime");
            if (StringUtils.isEmpty(smsCode)) {
                // 未收到登陆验证码
                if (StringUtils.isNotEmpty(lastSmsTime)
                        && nowTime - Long.parseLong(lastSmsTime) < 60 * 1000) {
                    // 如果发送的验证码到当前时间没有超过1分钟，则不重新发送验证码
                    logger.info("[" + phoneStr + "]no smscode would be send ,for time now="
                            + nowTime + " and last=" + lastSmsTime);
                    responseBean.setCode(ApiCode.INTERNAL_ERROR.getCode());
                    responseBean.setErrMsg("请等待1分钟后再次发送验证码");
                } else {
                    // 重新发送验证码
                    try {
                        smsCode = mainServer.conf().getSwitchOnlineSms()
                                ? "" + (new Random().nextInt(8999) + 1000)
                                : "1234";
                        smsAliService.sendChangePwdCode(phoneStr, smsCode);
                        if (smsCode != null && smsCode.length() > 0) {
                            stringRedisDao.insertOne(phoneStr + ".lastSmsTime",
                                                     nowTime + "",
                                                     5 * 60);
                            stringRedisDao.insertOne(phoneStr + ".smsCode", nowTime + "", 5 * 60);
                            return returnBean(responseBean);
                        } else {
                            responseBean.setCode(ApiCode.INTERNAL_ERROR.getCode());
                            return returnBean(responseBean);
                        }
                    } catch (Exception e) {
                        logger.error("error send smscode.", e);
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setErrMsg("传来的字段不正确");
                    }
                }
            } else {
                // 收到传来的登陆验证码
                long lastTime = Long.parseLong(lastSmsTime);
                String password = user.getPassword();
                if (StringUtils.isNotEmpty(lastSmsTime) && StringUtils.isNotEmpty(smsCodeSaved)
                        && StringUtils.isNotEmpty(password) && password.length() < 6
                        && smsCodeSaved.equals(smsCode) && nowTime - lastTime < 5 * 60 * 1000) {
                    // 验证码判断成功，可以执行密码修改
                    Map<String, Object> params = new HashMap<>();
                    params.put("phoneNum", phoneStr);
                    String encodePwd = authService.encodePassword(password);
                    params.put("password", encodePwd);
                    userService.dao().updateOne(user.getId(), params);
                    responseBean.setData("密码修改成功，请重新登陆");
                } else {
                    logger.info("[" + phoneStr + "]no smscode would be send ,for time now="
                            + nowTime + " and last=" + lastTime);
                    responseBean.setCode(ApiCode.INTERNAL_ERROR.getCode());
                    responseBean.setErrMsg("验证码已过期，请重新获取验证码");
                }
            }
        } else {
            logger.debug("user param error, no phoneNum detecetd" + user);
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("传来的字段不正确");
        }
        return returnBean(responseBean);
    }
}
