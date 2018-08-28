package com.pepper.common.config;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 根据是否配置使用负载均衡兼容使用域名调用和LOADBALANCED调用
 * 只有当配置了remote.loadBalancedServices[0]=xxx才使用LOADBALANCED调用否则使用域名调用
 */
public class LoadBalancedRestTemplate extends RestTemplate {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancedRestTemplate.class);

    @Autowired
    @Qualifier("getRestTemplate")
    private RestTemplate restTemplate;

    private RestTempalteClientProperties remoteClientProperties;

    public LoadBalancedRestTemplate(ClientHttpRequestFactory createFactory, RestTempalteClientProperties remoteClientProperties) {
        super(createFactory);
        this.remoteClientProperties = remoteClientProperties;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {

        Assert.notNull(url, "'url' must not be null");
        Assert.notNull(method, "'method' must not be null");
        Assert.notNull(url.getHost(), "'url.host' must not be null");

        List<String> remoteServiceList = remoteClientProperties.getLoadBalancedServices();

        List<String> resultList = new ArrayList<String>();
        if (null != remoteServiceList) {
            for (String s : remoteServiceList) {
                if (null != s) {
                    resultList.add(s.toUpperCase());
                }
            }
        }
        log.info("当前系统配置的所有需要使用负载均衡的服务有:{}",resultList);
        if (null != resultList && resultList.contains(url.getHost().toUpperCase())) {
            return super.doExecute(url, method, requestCallback, responseExtractor);// LOADBANLANCED调用
        }
        log.info("当前host={}没有配置负载均衡标识，使用域名方式调用", url.getHost());
        return restTemplate.execute(url, method, requestCallback, responseExtractor);// 默认域名方式调用
    }

}

