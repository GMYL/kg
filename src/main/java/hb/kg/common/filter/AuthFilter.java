package hb.kg.common.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hb.kg.common.bean.auth.HBUserSession;
import hb.kg.common.server.MainServer;
import hb.kg.common.service.AuthService;
import hb.kg.common.service.RoleService;
import hb.kg.common.util.set.HBStringUtil;
import hb.kg.system.bean.mongo.HBModule;
import hb.kg.system.service.ModuleService;

/**
 * 对用户权限进行校验，优先级在跨域之后的过滤器
 */
@Component
@Order(1)
@WebFilter(urlPatterns = "/**", filterName = "authFilter")
public class AuthFilter extends OncePerRequestFilter {
    @Autowired
    private MainServer mainServer;
    @Autowired
    private AuthService authService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ModuleService moduleService;

    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(mainServer.conf().getTokenHeader());
        if (HBStringUtil.isNotBlank(authHeader)) {
            try {
                // @WARN 注意request内的token是这个地方填充的
                request.setAttribute("usertoken", authHeader);
                // 先检查token是否是有效的（检查是否过期）
                HBUserSession session = authService.checkTokenValid(authHeader);
                // 如果获取session成功，那么说明用户是一个登陆OK的，只需要检查权限就可以了
                if (session != null) {
                    // @WARN 注意request内的session是在这个地方填充的
                    request.setAttribute("usersession", session);
                    // 从token中查找字段填充request
                    authService.fullfillRequestByToken(authHeader);
                    // 检查role是否符合
                    List<String> userrole = (List<String>) request.getAttribute("userrole");
                    Set<String> moduleIds = roleService.getAllModulesByRole(userrole);
                    Collection<HBModule> userModules = moduleService.dao().findAll(moduleIds);
                    { // 然后检查访问的接口是否有权限
                        boolean urlValid = false;
                        if (CollectionUtils.isNotEmpty(userModules)) {
                            for (HBModule module : userModules) {
                                if (moduleService.matches(module, request)) {
                                    urlValid = true;
                                    break;
                                }
                            }
                        }
                        if (!urlValid) {
                            response.sendError(403, "没有访问该接口的权限");
                            logger.warn("用户【" + request.getAttribute("username")
                                    + "】没有访问该接口的权限，正在访问" + request.getRequestURI());
                            return;
                        }
                    }
                    logger.debug("用户【" + request.getAttribute("username") + "】权限校验成功，正在访问"
                            + request.getRequestURI());
                    chain.doFilter(request, response);
                } else {
                    logger.error("从用户的数据中解析具体字段出错");
                    response.sendError(401, "登陆已过期，请重新生成");
                    return;
                }
            } catch (Exception e) {
                logger.error("从用户的数据中解析具体字段出错", e);
                return;
            }
        } else if (authService.isUrlAnonymously()) {
            // 如果访问url可以让匿名用户访问，则也可以继续filter
            chain.doFilter(request, response);
        } else if (HBStringUtil.endWith(request.getRequestURI(),
                                        ".jsp",
                                        ".html",
                                        ".css",
                                        ".js",
                                        ".dic",
                                        ".png",
                                        ".ttf",
                                        ".xml")
                || HBStringUtil.startWith(request.getRequestURI(),
                                          moduleService.getUrlPrefix() + "/webjars",
                                          moduleService.getUrlPrefix() + "/v2/api-docs",
                                          moduleService.getUrlPrefix() + "/swagger-resources")) {
            // 放过对静态文件的请求
            chain.doFilter(request, response);
        } else if (request.getMethod().equals("OPTIONS")) {
            // 放过对OPTIONS的请求
            chain.doFilter(request, response);
        } else {
            response.sendError(401, "您未登录，请登陆后访问该接口");
            logger.debug("用户" + request.getRemoteAddr() + "无权访问页面" + request.getRequestURI());
            return;
        }
    }
}
