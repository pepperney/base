package com.pepper.common.config;



import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.pepper.common.security.MyHeaderInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:
 */
@Configuration
@ConditionalOnClass({RestTemplate.class, HttpClient.class})
@EnableConfigurationProperties({RestTempalteClientProperties.class, SecurityProperties.class})
public class RestTemplateAutoConfiguration implements EnvironmentAware {

    private Environment env;

    @Autowired
    private RestTempalteClientProperties remoteClientProperties;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Bean
    @ConditionalOnMissingBean(name = "getRestTemplate")
    @Primary
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(createFactory());
        this.setConverterList(restTemplate);
        List<ClientHttpRequestInterceptor> ins = restTemplate.getInterceptors();
        ins.add(new MyHeaderInterceptor(securityProperties, getRequestSourceApp()));
        restTemplate.setInterceptors(ins);
        return restTemplate;
    }

    private String getRequestSourceApp() {
        try {
            String system = env.getProperty("spring.application.name");
            if (!StringUtils.hasText(system)) {
                String logPath = env.getProperty("logging.path");
                if (StringUtils.hasText(logPath)) {
                    int index = logPath.lastIndexOf("/");
                    return logPath.substring(index + 1, logPath.length());
                }
            } else {
                return new StringBuffer(system).append(".pepper.com").toString();
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    @LoadBalanced
    @Qualifier("loadBalancedRestTemplate")
    @Bean(name = "loadBalancedRestTemplate")
    @ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
    @ConditionalOnClass({RestTemplate.class, HttpClient.class, RibbonLoadBalancerClient.class})
    public LoadBalancedRestTemplate myRestTemplate() {
        LoadBalancedRestTemplate restTemplate = new LoadBalancedRestTemplate(createFactory(), remoteClientProperties);
        this.setConverterList(restTemplate);
        List<ClientHttpRequestInterceptor> ins = restTemplate.getInterceptors();
        ins.add(new MyHeaderInterceptor(securityProperties, getRequestSourceApp()));
        restTemplate.setInterceptors(ins);
        return restTemplate;
    }

    private void setConverterList(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();
        HttpMessageConverter<?> converterTarget = null;
        for (HttpMessageConverter<?> item : converterList) {
            if (item.getClass() == StringHttpMessageConverter.class) {
                converterTarget = item;
                break;
            }
        }
        if (converterTarget != null) {
            converterList.remove(converterTarget);
        }

        HttpMessageConverter<?> converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converterList.add(1, converter);
    }

    private ClientHttpRequestFactory createFactory() {
        if (remoteClientProperties.getMaxTotalConn() > 0) {
            HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(remoteClientProperties.getMaxTotalConn())
                    .setMaxConnPerRoute(remoteClientProperties.getMaxConnPerRoute()).evictExpiredConnections()
                    .evictIdleConnections((remoteClientProperties.getMaxIdleTime()), TimeUnit.MILLISECONDS).build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(remoteClientProperties.getConnectTimeout());
            factory.setReadTimeout(remoteClientProperties.getReadTimeout());
            return factory;
        } else {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(remoteClientProperties.getConnectTimeout());
            factory.setReadTimeout(remoteClientProperties.getReadTimeout());
            return factory;
        }
    }
}
