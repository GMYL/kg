package hb.kg.common.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import hb.kg.common.bean.auth.HBRole;
import hb.kg.common.dao.BaseDao;
import hb.kg.common.dao.RoleDao;

@Service
public class RoleService extends BaseCRUDService<HBRole> {
    @Resource
    private RoleDao roleDao;

    @Override
    public BaseDao<HBRole> dao() {
        return roleDao;
    }

    public Set<String> getAllModulesByRole(List<String> roles) {
        Set<String> userModules = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roles)) {
            Collection<HBRole> roleBeans = roleDao.findAll(roles);
            if (CollectionUtils.isNotEmpty(roleBeans)) {
                for (HBRole role : roleBeans) {
                    if (CollectionUtils.isNotEmpty(role.getModules())) {
                        userModules.addAll(role.getModules());
                    }
                }
            }
        }
        return userModules;
    }
}
