package hb.kg.user.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.common.service.AuthService;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.service.RoleService;
import hb.kg.common.util.id.HeadUtil;
import hb.kg.common.util.json.JSONObject;
import hb.kg.user.bean.enums.HBRoleEnum;
import hb.kg.user.bean.http.HBUserAdmin;
import hb.kg.user.bean.http.HBUserBasic;
import hb.kg.user.bean.http.HBUserLogin;
import hb.kg.user.bean.mongo.HBUser;
import hb.kg.user.dao.UserDao;

@Service
public class UserService extends BaseCRUDService<HBUser> {
    @Resource
    private UserDao userDao;
    @Autowired
    private AuthService authService;

    public BaseMongoDao<HBUser> dao() {
        return userDao;
    }

    @Autowired
    public RoleService roleService;
    @Autowired
    private UserGroupService userGroupService;

    // 20180216!!!这里今后要改为HBUserRegister的生成方式，凡是Register要求的字段都要填满
    public HBUser initNewHBUser(Map<String, Object> data) {
        HBUser user = new HBUser();
        // 以下条目是新增用户的必备条件，三者必须满足一个，否则不允许新增用户
        if (data.get("userName") != null || data.get("phoneNum") != null
                || data.get("email") != null) {
            user.prepareHBBean();
            if (data.get("userName") != null) {
                user.setUserName(data.get("userName").toString());
            }
            if (data.get("phoneNum") != null) {
                user.setPhoneNum(data.get("phoneNum").toString());
                user.setPhoneState(true);
            }
            if (data.get("nickName") != null) {
                user.setNickName(data.get("nickName").toString());
            }
            if (data.get("trueName") != null) {
                user.setTrueName(data.get("trueName").toString());
            }
            if (data.get("gender") != null) {
                user.setGender(data.get("gender").toString());
            }
            if (data.get("group") != null) {// 初始普通组
                List<String> list = new ArrayList<>();
                list.add(data.get("group").toString());
                user.setGroup(list);
            }
            user.setRegDate(new Date());
            user.setValid(data.containsKey("valid") ? (boolean) data.get("valid") : true);
            if (data.get("password") != null) {
                user.setPassword(authService.encodePassword(data.get("password").toString()));
                user.setLastPasswordResetDate(new Date());
            }
            if (data.get("email") != null) {
                user.setEmail(data.get("email").toString());
                user.setEmailState(false);
            }
            user.setLogo(HeadUtil.initHead(user.getGender()));
            user.setLevel(data.containsKey("level") ? (int) data.get("level") : 1); // 从1级开始
            user.setVip(data.containsKey("vip") ? (int) data.get("vip") : 0); // 会员状态从非会员开始
        }
        return user;
    }

    public boolean checkIfExist(String key,
                                String value) {
        HBUser hbUser = null;
        if ("all".equals(key)) {
            hbUser = userDao.findUserByNameOrPhoneOrEmail(value);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put(key, value);
            hbUser = userDao.findOne(params);
        }
        return hbUser == null ? false : true;
    }

    public void updateUserFull(HBUser user) {
        if (StringUtils.isNotEmpty(user.getId())) {
            userDao.updateOne(user.getId(), user.toMongoHashMap());
        }
    }

    // "birthday", "age", "email",
    // "logo","nickName","trueName","profile","comment","gendar"
    public void updateUserLimit(HBUser user) {
        if (StringUtils.isNotEmpty(user.getId())) {
            userDao.updateOne(user.getId(), user.toMongoHashMap());
        }
    }

    public HBUser getUserInfoByUsername(String userName) {
        return userDao.findUserByNameOrPhoneOrEmail(userName);
    }

    public HBUserBasic getUserBasicInfo(String userid) {
        return mongoTemplate.findOne(new Query(Criteria.where("id").is(userid)),
                                     HBUserBasic.class,
                                     userDao.getCollectionName());
    }

    public HBUserLogin getLoginUserInfo(String userid) {
        HBUserLogin user = mongoTemplate.findOne(new Query(Criteria.where("id").is(userid)),
                                                 HBUserLogin.class,
                                                 userDao.getCollectionName());
        Set<String> roles = getRoleSetByGroups(user.getGroup());
        if (CollectionUtils.isNotEmpty(user.getRoles())) {
            roles.addAll(user.getRoles());
        }
        List<String> modules = getModuleListByRoles(new ArrayList<>(roles));
        user.setModules(modules);
        return user;
    }

    public String getSessionCode() {
        return RandomStringUtils.random(9, true, true);
    }

    public List<HBUserAdmin> getAllAdmin() {
        Query query = Query.query(Criteria.where("roles").in(HBRoleEnum.STAFF_ROLE.getName()));
        return mongoTemplate.find(query, HBUserAdmin.class);
    }

    public List<String> getRoleListByGroups(List<String> groups) {
        return new ArrayList<>(getRoleSetByGroups(groups));
    }

    public Set<String> getRoleSetByGroups(List<String> groups) {
        Set<String> roles = new HashSet<>();
        if (CollectionUtils.isNotEmpty(groups)) {
            roles.addAll(userGroupService.getAllUserRolesByGroup(groups));
        }
        return roles;
    }

    public List<String> getModuleListByRoles(List<String> roles) {
        return new ArrayList<>(getModuleSetByRoles(roles));
    }

    public Set<String> getModuleSetByRoles(List<String> roles) {
        Set<String> modules = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roles)) {
            modules.addAll(roleService.getAllModulesByRole(roles));
        }
        return modules;
    }

    public int checkVip(String userid) {
        if (StringUtils.isNotEmpty(userid)) {
            HBUser user = dao().findOne(userid);
            return user.getVip() != null ? user.getVip() : 0;
        }
        return -1;
    }

    /**
     * 记录用户上次登陆的日期
     */
    public void recordUserLogin(String userid,
                                String remoteIp) {
        if (StringUtils.isNotEmpty(userid)) {
            mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(userid)),
                                      new Update().set("lastLoginDate", new Date())
                                                  .set("lastLoginIp", remoteIp)
                                                  .inc("totalLoginTime", 1),
                                      HBUser.class);
        }
    }

    /**
     * 解决其它模块数据调用，这里可以加入截断，对用户的调用数据进行截断并本类的特有功能
     */
    public Object solveData(JSONObject data) {
        return super.solveData(data);
    }
}
