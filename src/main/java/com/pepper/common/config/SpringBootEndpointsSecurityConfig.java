package com.pepper.common.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.pepper.common.security.MyAuthenticationProvider;
import com.pepper.common.security.ProtectResourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:支持springboot endpoint 使用Spring-Security实现权限控制(只有endpoint的URL以及配置受保护的资源才进行权限控制
 */
@Configuration
@EnableConfigurationProperties({ManagementServerProperties.class, ProtectResourceProperties.class,SecurityProperties.class})
@Order(101)
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
public class SpringBootEndpointsSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SpringBootEndpointsSecurityConfig.class);

    @Autowired
    private ManagementServerProperties managementServerProperties;

    @Autowired
    private ProtectResourceProperties protectResourceProperties;

    @Value("${druid.security.flag:false}")
    private boolean druidSecurity;

    @Autowired
    private SecurityProperties securityProperties;

    /**
     * 如果现有系统已经有springsecurity配置，则 需要重写该方法
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(buildAuthenticationProvider());
    }

    /**
     * 如果现有系统已经有springsecurity配置，则需要重写该方法
     *
     * @return
     */
    protected AuthenticationProvider buildAuthenticationProvider() {
        return new MyAuthenticationProvider(securityProperties, userDetailsService(), protectResourceProperties);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        http.csrf().disable();

        if (druidSecurity) {
            setDruidSecurity(http);
        }

        String contextPath = managementServerProperties.getContextPath();
        if (!contextPath.startsWith("/") || contextPath.endsWith("/")) {
            throw new IllegalArgumentException("management.contextPath partten is error!" + contextPath);
        }
        if (managementServerProperties.getSecurity().isEnabled()) {
            http.authorizeRequests().antMatchers(contextPath + "/**").authenticated();
        }

        List<String> protectedResources = this.protectResourceProperties.getUrl();
        for (String resource : protectedResources) {
            if (!resource.startsWith("/") || resource.endsWith("/")) {
                throw new IllegalArgumentException("protectedResources partten is error!" + resource);
            }
            http.authorizeRequests().antMatchers(resource + "/**").authenticated();
            log.info("{}/**需要安全认证", resource);
        }

        if (managementServerProperties.getSecurity().isEnabled() || !protectedResources.isEmpty()) {
            http.httpBasic();
        }
    }

    /**
     * 检查是否配置了Druid Servlet 配置了且用户密码未设置，使用spring security 过滤权限
     *
     * @param http
     * @throws Exception
     */
    private void setDruidSecurity(HttpSecurity http) {
        try {
            String[] servletNames = super.getApplicationContext().getBeanNamesForType(ServletRegistrationBean.class);
            if (null == servletNames || servletNames.length == 0) {
                return;
            }
            for (String name : servletNames) {
                Object servletBean = super.getApplicationContext().getBean(name);
                if (servletBean instanceof ServletRegistrationBean) {
                    ServletRegistrationBean servlet = (ServletRegistrationBean) servletBean;
                    if ("statViewServlet".equalsIgnoreCase(servlet.getServletName())
                            || "resourceServlet".equalsIgnoreCase(servlet.getServletName())) {
                        Collection<String> mappingUrlList = servlet.getUrlMappings();
                        Map<String, String> initParamMap = servlet.getInitParameters();
                        if (!initParamMap.containsKey("loginUsername") && !initParamMap.containsKey("loginPassword")) {
                            for (String mappingUrl : mappingUrlList) {
                                log.info("druid spring security path is : [{}]", mappingUrl);
                                http.authorizeRequests().antMatchers(mappingUrl).authenticated();
                            }
                        } else {
                            log.info("应用自己配置了druid用户密码，不使用spring security 进行过滤权限");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("spring security 设置失败 :{}", e.getMessage());
        }
    }



    /**
     * 支持自定义系统间的资源权限设置
     *
     * @param http
     * @param properties
     * @param protectResourceProperties
     * @throws Exception
     */
    public static void setOpsmonitorAndProtectResource(HttpSecurity http, ManagementServerProperties properties,
                                                       ProtectResourceProperties protectResourceProperties) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();
        String contextPath = properties.getContextPath();
        if (!contextPath.startsWith("/") || contextPath.endsWith("/")) {
            throw new IllegalArgumentException("management.contextPath partten is error!" + contextPath);
        }
        if (properties.getSecurity().isEnabled()) {
            http.authorizeRequests().antMatchers(contextPath + "/**").authenticated();
        }

        List<String> protectedResources = protectResourceProperties.getUrl();
        for (String resource : protectedResources) {
            if (!resource.startsWith("/") || resource.endsWith("/")) {
                throw new IllegalArgumentException("protectedResources partten is error!" + resource);
            }
            http.authorizeRequests().antMatchers(resource + "/**").authenticated();
            log.info("{}/**需要安全认证", resource);
        }

        if (properties.getSecurity().isEnabled() || !protectedResources.isEmpty()) {
            http.httpBasic();
        }
    }
}