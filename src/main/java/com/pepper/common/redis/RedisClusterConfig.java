package com.pepper.common.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:
 */
@Configuration
@ConditionalOnClass({JedisCluster.class})
@EnableConfigurationProperties(RedisClusterProperties.class)
public class RedisClusterConfig {
    @Autowired
    private RedisClusterProperties redisClusterProperties;

    @Bean
    public JedisCluster jedisCluster() {
        Set<HostAndPort> nodes = new HashSet<>();
        for (String node : redisClusterProperties.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new HostAndPort(parts[0], Integer.valueOf(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis cluster nodes " + "property '" + node + "'", ex);
            }
        }

        if (nodes.isEmpty()) {
            return null;
        } else {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(redisClusterProperties.getMaxTotal());
            config.setMaxIdle(redisClusterProperties.getMaxIdle());
            config.setMinIdle(redisClusterProperties.getMinIdle());
            config.setMaxWaitMillis(redisClusterProperties.getMaxWaitMillis());
            return new JedisCluster(nodes, redisClusterProperties.getConnectionTimeout(), redisClusterProperties.getSoTimeout(),
                    redisClusterProperties.getMaxRedirections(), config);
        }
    }
}

