package com.pepper.common.security;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:白名单的认证权限控制器
 */
public class MyAuthenticationProvider extends DaoAuthenticationProvider {

    private static Logger logger = LoggerFactory.getLogger(MyAuthenticationProvider.class);

    private SecurityProperties securityProperties;

    private ProtectResourceProperties protectResourceProperties;

    public MyAuthenticationProvider(SecurityProperties securityProperties, UserDetailsService userDetailsService, ProtectResourceProperties protectResourceProperties) {
        this.securityProperties = securityProperties;
        this.protectResourceProperties = protectResourceProperties;
        super.setUserDetailsService(userDetailsService);
    }

    /**
     * 自定义验证方式
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {

            if (null == RequestContextHolder.getRequestAttributes()) {
                logger.info("RequestContextHolder.getRequestAttributes() is null " + RequestContextHolder.getRequestAttributes());
                return whiteAuth();
            }

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            String sourceApp = request.getHeader(MyHeaderInterceptor.REQUEST_SOURCE_APP);
            String ip = getClientIP(request);

            // 当前系统是白名单或IP是白名单，无需权限认证直接访问
            if (protectResourceProperties.getWhiteDomain().contains(sourceApp) || protectResourceProperties.getWhiteIp().contains(ip)) {
                return whiteAuth();
            }
        } catch (Exception e) {
            logger.error("authentication error", e);
            return whiteAuth();
        }

        return super.authenticate(authentication);

    }

    private UsernamePasswordAuthenticationToken whiteAuth() {
        List<GrantedAuthority> authList = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ACTUATOR,ROLE_USER");
        UserDetails user = new User(securityProperties.getUser().getName(), securityProperties.getUser().getPassword(), authList);
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        return new UsernamePasswordAuthenticationToken(user, securityProperties.getUser().getPassword(), authorities);
    }

    @Override
    public boolean supports(Class<?> arg0) {
        return true;
    }

    public static String getClientIP(HttpServletRequest request) {
        String IP = request.getHeader("x-forwarded-for");
        if (null == IP || 0 == IP.length() || "unknown".equalsIgnoreCase(IP)) {
            IP = request.getHeader("Proxy-Client-IP");
        }
        if (null == IP || 0 == IP.length() || "unknown".equalsIgnoreCase(IP)) {
            IP = request.getHeader("WL-Proxy-Client-IP");
        }
        if (null == IP || 0 == IP.length() || "unknown".equalsIgnoreCase(IP)) {
            IP = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况,第一个IP为客户端真实IP
        // 多个IP会按照','分割('***.***.***.***'.length()=15)
        if (null != IP && IP.length() > 15) {
            if (IP.indexOf(",") > 0) {
                IP = IP.substring(0, IP.indexOf(","));
            }
        }
        return IP;
    }

}
