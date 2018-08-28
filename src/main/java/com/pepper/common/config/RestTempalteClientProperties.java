package com.pepper.common.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = RestTempalteClientProperties.PREFIX)
public class RestTempalteClientProperties {

    public static final String PREFIX = "remote";

    private int maxTotalConn = 0; // 最大链接数, 默认为0, 使用simple模式

    private int maxConnPerRoute = 100; // 单URL并发链接数

    private int connectTimeout = 2000; // 建立链接超时 2s

    private int readTimeout = 30000; // 数据读取超时 30s

    private long maxIdleTime = 10000;// 连接池剔除空闲连接间隔时间 10s

    private List<String> loadBalancedServices = new ArrayList<>();

    public int getMaxTotalConn() {
        return maxTotalConn;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public void setMaxTotalConn(int maxTotalConn) {
        this.maxTotalConn = maxTotalConn;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public List<String> getLoadBalancedServices() {
        return loadBalancedServices;
    }

    public void setLoadBalancedServices(List<String> loadBalancedServices) {
        this.loadBalancedServices = loadBalancedServices;
    }
}
