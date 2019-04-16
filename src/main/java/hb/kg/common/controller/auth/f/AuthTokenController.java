package hb.kg.common.controller.auth.f;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hb.kg.common.bean.auth.HBUserSession;
import hb.kg.common.bean.enums.ApiCode;
import hb.kg.common.bean.http.ResponseBean;
import hb.kg.common.controller.BaseController;
import hb.kg.common.dao.StringRedisDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.util.id.ValidateUtil;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.msg.service.SmsAliService;
import hb.kg.user.bean.http.HBUserBasic;
import hb.kg.user.bean.http.HBUserLogin;
import hb.kg.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "[F]用户Token")
@RestController
@RequestMapping(value = { "/${api.version}/f/auth/token", "/${api.version}/b/auth/token" })
public class AuthTokenController extends BaseController {
    @Autowired
    private AuthService authService;
    @Autowired
    private SmsAliService smsAliService;
    @Autowired
    private UserService userService;
    @Resource
    private StringRedisDao stringRedisDao;

    /**
     * 使用手机验证码登陆
     */
    @ApiOperation(value = "使用手机验证码登陆", notes = "使用手机验证码登陆", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/getByAuthCode", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseBean getTokenByPS(@ApiParam(value = "用户信息") @RequestBody HBUserLogin user) {
        ResponseBean responseBean = getReturn();
        // 使用验证码登陆
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
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("请等待1分钟后再次发送验证码");
                } else {
                    // 发送验证码
                    try {
                        smsCode = mainServer.conf().getSwitchOnlineSms()
                                ? "" + (new Random().nextInt(8999) + 1000)
                                : "1234";
                        smsAliService.sendLoginSmsCode(phoneStr, smsCode);
                        if (smsCode != null && smsCode.length() > 0) {
                            stringRedisDao.insertOne(phoneStr + ".lastSmsTime",
                                                     nowTime + "",
                                                     5 * 60);
                            stringRedisDao.insertOne(phoneStr + ".smsCode", nowTime + "", 5 * 60);
                            return returnBean(responseBean);
                        } else {
                            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                            responseBean.setErrMsg("手机验证码校验失败");
                            return returnBean(responseBean);
                        }
                    } catch (Exception e) {
                        logger.error("error send smscode.", e);
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setErrMsg("手机验证码校验失败");
                    }
                }
            } else {
                // 收到传来的登陆验证码
                if (smsCodeSaved != null) {
                    long lastTime = Long.parseLong(lastSmsTime);
                    if (StringUtils.isNotEmpty(lastSmsTime) && StringUtils.isNotEmpty(smsCodeSaved)
                            && smsCodeSaved.equals(smsCode) && nowTime - lastTime < 5 * 60 * 1000) {
                        // 登陆成功
                        HBUserSession session = authService.loginByPhone(phoneStr);
                        if (session != null && StringUtils.isNotEmpty(session.getToken())) {
                            Map<String, Object> map;
                            try {
                                map = PropertyUtils.describe(userService.getUserInfoByUsername(user.getUserName()));
                                map.put("jwtToken", session.getToken());
                                responseBean.setData(map);
                            } catch (Exception e) {
                                logger.info("[" + phoneStr
                                        + "]no smscode would be send ,for time now=" + nowTime
                                        + " and last=" + lastTime);
                                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                                responseBean.setErrMsg("用户没有注册，请先注册");
                            }
                        } else {
                            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        }
                    } else {
                        logger.info("[" + phoneStr + "]no smscode would be send ,for time now="
                                + nowTime + " and last=" + lastTime);
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setErrMsg("验证码已过期，请重新获取验证码");
                    }
                }
            }
        } else {
            logger.debug("user param error, no phoneNum detecetd" + user);
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("用户没有传递手机号，登陆失败");
        }
        return returnBean(responseBean);
    }

    /**
     * 使用用户名/手机号密码登陆
     */
    @ApiOperation(value = "使用用户名密码登陆", notes = "使用用户名密码登陆", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/login", method = { RequestMethod.POST })
    public ResponseBean getTokenByNP(@ApiParam(value = "用户信息") @RequestBody HBUserLogin hbuser) {
        ResponseBean responseBean = getReturn();
        // 暂时先用手机号码登陆 by whs
        String username = hbuser.getUserName();
        String password = hbuser.getPassword();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            try {
                HBUserSession session = authService.login(username, password);
                if (session != null) {
                    hbuser = userService.getLoginUserInfo(session.getUserid());
                    hbuser.setJwtToken(session.getToken());
                    responseBean.setData(hbuser);
                    userService.recordUserLogin(hbuser.getId(), request.getRemoteAddr());
                } else {
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("用户名密码不正确");
                }
            } catch (Exception e) {
                logger.debug("登陆失败", e);
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("用户名密码不正确");
            }
        } else {
            logger.debug("用户传来的信息格式错误" + hbuser);
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("登陆信息格式错误");
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "刷新Token值", notes = "刷新Token值，用旧的 token值 换取新的 token值", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/refresh", method = { RequestMethod.GET })
    public ResponseBean refreshToken(HttpServletRequest request) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (HBStringUtil.isNotBlank(userid)) {
            HBUserSession session = authService.loginById(userid);
            responseBean.setOneData("jwtToken", session.getToken());
        } else {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        }
        return returnBean(responseBean);
    }

    @ApiOperation(value = "刷新用户信息", notes = "刷新用户信息", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/refreshUser", method = { RequestMethod.GET })
    public ResponseBean refreshUser(HttpServletRequest request) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        HBUserLogin hbuser = userService.getLoginUserInfo(userid);
        responseBean.setData(hbuser);
        // 注意这里没有去刷新jwtToken，也没有修改redis中的内容，可能引起数据不一致
        return returnBean(responseBean);
    }

    /**
     * 使用用户ID密码登陆
     */
    @ApiOperation(value = "使用用户ID密码登陆", notes = "使用用户ID密码登陆", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/checkPasswordValid", method = { RequestMethod.POST })
    public ResponseBean checkPasswordValid(@ApiParam(value = "用户信息") @RequestBody HBUserLogin hbuser) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        String password = hbuser.getPassword();
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(password)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            responseBean.setData(authService.checkPasswordValid(userid, password) != null);
        }
        return returnBean(responseBean);
    }

    /**
     * 微信检查openid
     */
    @ApiOperation(value = "微信检查openid是否对应", notes = "微信检查openid是否对应，手机号和openID", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/checkOpenId")
    public ResponseBean checkOpenId(@ApiParam(value = "后台对接微信的密钥") @RequestHeader("Open-secretKey") String secretKey,
                                    @ApiParam(value = "微信openid") @RequestParam(value = "openid") String wcid,
                                    @ApiParam(value = "手机号") @RequestParam(value = "phoneNum") String num) {
        ResponseBean responseBean = getReturn();
        try {
            String secretKeyDecode = URLDecoder.decode(secretKey, "utf-8");
            if (!secretKeyDecode.equals(mainServer.conf().getRobotPassword())) {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("参数传递错误");
                return returnBean(responseBean);
            } else if (StringUtils.isNotEmpty(wcid) && StringUtils.isNotEmpty(num)) {
                HBUserLogin hbUserLogin = mongoTemplate.findOne(new Query(Criteria.where("openId")
                                                                                  .is(wcid)
                                                                                  .and("valid")
                                                                                  .is(true)
                                                                                  .and("phoneNum")
                                                                                  .is(num)),
                                                                HBUserLogin.class);
                if (hbUserLogin != null) {
                    HBUserSession session = authService.loginByName(hbUserLogin.getUserName());
                    if (session != null && StringUtils.isNotEmpty(session.getToken())) {
                        Map<String, Object> map = new HashMap<>();
                        try {
                            map.put("userName", hbUserLogin.getUserName());
                            map.put("nickName", hbUserLogin.getNickName());
                            map.put("jwtToken", session.getToken());
                            responseBean.setData(map);
                        } catch (Exception e) {
                            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                            responseBean.setErrMsg("用户没有注册，请先注册");
                        }
                    } else {
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    }
                } else {
                    responseBean.setCode(ApiCode.PARAM_FORMAT_ERROR.getCode());
                }
            } else {
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("excep parse error!", e);
            responseBean.setErrMsg("验证不通过");
        }
        return returnBean(responseBean);
    }

    /**
     * 微信使用手机号、密码登陆
     */
    @ApiOperation(value = "微信使用手机号、密码登陆", notes = "微信使用手机号、密码登陆", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "hbjwtauth", value = "用户权限验证", required = true) })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/loginWeChat", method = { RequestMethod.POST })
    public ResponseBean loginWeChat(@ApiParam(value = "用户信息( 需要包含 手机号、密码 )") @RequestBody HBUserLogin hbuser) {
        ResponseBean responseBean = getReturn();
        String username = hbuser.getUserName();
        String password = hbuser.getPassword();
        String openid = hbuser.getOpenId();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)
                && StringUtils.isNotEmpty(openid)) {
            try {
                HBUserBasic hbUserLogin = mongoTemplate.findOne(new Query(Criteria.where("phoneNum")
                                                                                  .is(username)
                                                                                  .and("valid")
                                                                                  .is(true)),
                                                                HBUserBasic.class);
                if (hbUserLogin != null) {
                    HBUserSession session = authService.login(hbUserLogin.getUserName(), password);
                    if (session != null && StringUtils.isNotEmpty(session.getToken())
                            && StringUtils.isNotEmpty(hbuser.getId())) {
                        // 增加opendid
                        Map<String, Object> params = new HashMap<>();
                        params.put("openId", openid);
                        userService.dao().updateOne(session.getUserid(), params);
                        hbuser = userService.getLoginUserInfo(session.getUserid());
                        hbuser.setJwtToken(session.getToken());
                        responseBean.setData(hbuser);
                        userService.recordUserLogin(hbuser.getId(), request.getRemoteAddr());
                    } else {
                        responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                        responseBean.setErrMsg("用户名密码不正确");
                    }
                } else {
                    responseBean.setCode(ApiCode.PARAM_FORMAT_ERROR.getCode());
                    responseBean.setErrMsg("用户名密码不正确");
                }
            } catch (Exception e) {
                logger.debug("登陆失败", e);
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("用户名密码不正确");
            }
        } else {
            logger.debug("用户传来的信息格式错误" + hbuser);
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("登陆信息格式错误");
        }
        return returnBean(responseBean);
    }
}
