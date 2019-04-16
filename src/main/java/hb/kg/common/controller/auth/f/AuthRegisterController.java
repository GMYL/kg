package hb.kg.common.controller.auth.f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;

import hb.kg.common.bean.auth.HBUserSession;
import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.KeyValuePair;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseController;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.util.id.ValidateUtil;
import hb.kg.msg.service.SmsAliService;
import hb.kg.user.bean.http.HBUserLogin;
import hb.kg.user.bean.http.HBUserRegister;
import hb.kg.user.bean.mongo.HBUser;
import hb.kg.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "[F]用户注册")
@RestController
@RequestMapping(value = "/${api.version}/f/auth/register")
public class AuthRegisterController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private SmsAliService smsAliService;
    @Autowired
    private AuthService authService;
    @Resource
    private StringRedisDao stringRedisDao;

    // 判断用户是否存在
    @ApiOperation(value = "判断用户是否存在", notes = "判断用户是否存在", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @RequestMapping(value = "/checkUserExist", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean checkIfUserExist(@ApiParam(value = "查询条件") @RequestBody KeyValuePair<String, Object> kv) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isEmpty(kv.getKey()) || StringUtils.isEmpty("" + kv.getValue())) {
            responseBean.setErrMsg("传递数据格式有误");
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            return returnBean(responseBean);
        } else {
            responseBean.setOneData("exist",
                                    userService.checkIfExist(kv.getKey(),
                                                             kv.getValue().toString()));
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "发送手机验证码", notes = "发送手机验证码", produces = "application/json")
    @RequestMapping(value = "/smscode", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean sendSms(@ApiParam(value = "用户信息") @RequestBody HBUserLogin user) {
        ResponseBean responseBean = getReturn();
        // 判断手机号码是否输入
        String phoneStr = user.getPhoneNum();
        if (!ValidateUtil.isChinaPhoneNum(phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
        } else {
            // 判断是否重复发送验证码
            long nowTime = System.currentTimeMillis();
            String lastTimeStr = stringRedisDao.findOne(phoneStr + ".regSmsCodeTime");
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
                        ? "" + (new Random().nextInt(8999) + 1000)
                        : "1234";
                SendSmsResponse smsResponse = smsAliService.sendRegisterCode(phoneStr, smsCode);
                if ("OK".equals(smsResponse.getCode())) {
                    stringRedisDao.insertOne(phoneStr + ".regSmsCode", smsCode, 5 * 60);
                    stringRedisDao.insertOne(phoneStr + ".regSmsCodeTime",
                                             System.currentTimeMillis() + "",
                                             5 * 60);
                    stringRedisDao.insertOne(phoneStr + ".phoneNum", phoneStr);
                } else {
                    responseBean.setErrMsg(smsResponse.getCode());
                    responseBean.setCode(ApiCode.INTERNAL_ERROR.getCode());
                }
            } catch (Exception e) {
                logger.error("发送短信错误：用户手机号[" + phoneStr + "]", e);
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 验证过验证码后，需要设置密码
     */
    @ApiOperation(value = "用户注册", notes = "验证过验证码后，需要设置密码", produces = "application/json")
    @RequestMapping(value = "", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean registerBySmsCheck(@ApiParam(value = "用户注册信息") @RequestBody HBUserRegister hbUser) {
        ResponseBean responseBean = getReturn();
        String regPassword = hbUser.getPassword();
        String phoneStr = hbUser.getPhoneNum();
        if (StringUtils.isEmpty(regPassword) || regPassword.length() < 6) {
            // 输入的密码格式错误
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("输入密码格式错误");
        } else if (!ValidateUtil.isChinaPhoneNum(phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("手机号码格式错误");
        } else if (userService.checkIfExist("phoneNum", phoneStr)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("手机号码已存在");
        } else {
            String smsRegCode = stringRedisDao.findOne(phoneStr + ".regSmsCode");
            // String smsRegCode = "1234"; //测试用
            if (StringUtils.isEmpty(smsRegCode) || StringUtils.isEmpty(hbUser.getSmsCode())
                    || !smsRegCode.equals(hbUser.getSmsCode())) {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("验证码输入错误");
            } else {
                stringRedisDao.insertOne(phoneStr + ".regSmsCodeTime",
                                         System.currentTimeMillis() - 60 * 3600 + "");// gmy
                Map<String, Object> map = new HashMap<>();
                map.put("phoneNum", phoneStr);
                map.put("password", regPassword);
                map.put("group", "normal");
                HBUser user = userService.initNewHBUser(map);
                userService.dao().insert(user);
                HBUserSession session = authService.findOne(user.getUserName(), "name");
                responseBean.setOneData("jwtToken", authService.generateToken(session).getToken());
                logger.info("add new User:" + user.toString());
            }
        }
        return returnBean(responseBean);
    }
}
