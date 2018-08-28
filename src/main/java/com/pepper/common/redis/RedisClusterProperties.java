package com.pepper.common.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:
 */
@ConfigurationProperties(prefix = RedisClusterProperties.PREFIX)
public class RedisClusterProperties {
    public static final String PREFIX = "redis";

    private int connectionTimeout = 2000;
    private int soTimeout = 5000;
    private int maxRedirections = 5;

    private int maxTotal = 8;
    private int maxIdle = 8;
    private int minIdle = 0;
    private long maxWaitMillis = 30 * 1000L;

    private List<String> nodes = new ArrayList<String>();

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getMaxRedirections() {
        return maxRedirections;
    }

    public void setMaxRedirections(int maxRedirections) {
        this.maxRedirections = maxRedirections;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }
}

