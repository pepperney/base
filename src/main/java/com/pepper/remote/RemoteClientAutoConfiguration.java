package com.pepper.remote;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
@Configuration
@ConditionalOnClass({HttpClient.class})
public class RemoteClientAutoConfiguration {


    @Configuration
    @ConditionalOnMissingClass("org.springframework.cloud.client.loadbalancer.LoadBalancerClient")
    @EnableConfigurationProperties({RemoteClientProperties.class, SecurityProperties.class})
    protected static class RemoteClientBeanPostProcessorConfig {
        @Autowired
        private RemoteClientProperties remoteClientProperties;
        @Autowired
        private SecurityProperties securityProperties;

        @Bean
        public RemoteClientBuilder remoteClientBuilder() {
            return new RemoteClientBuilder(securityProperties, remoteClientProperties);
        }

        @Bean
        public BeanPostProcessor beanPostProcessor() {
            return new RemoteClientBeanPostProcessor(remoteClientBuilder());
        }
    }

    @Configuration
    @ConditionalOnClass(LoadBalancerClient.class)
    @EnableConfigurationProperties({RemoteClientProperties.class, SecurityProperties.class})
    protected static class RibbonRemoteClientBeanPostProcessorConfig {
        @Autowired
        private RemoteClientProperties remoteClientProperties;

        @Autowired
        private LoadBalancerClient loadBalancerClient;

        @Autowired
        private SecurityProperties securityProperties;

        @Bean
        public RemoteClientBuilder remoteClientBuilder() {
            return new RemoteClientBuilder(securityProperties, remoteClientProperties, loadBalancerClient);
        }

        @Bean
        public BeanPostProcessor ribbonBeanPostProcessor() {
            return new RemoteClientBeanPostProcessor(remoteClientBuilder());
        }
    }


}
