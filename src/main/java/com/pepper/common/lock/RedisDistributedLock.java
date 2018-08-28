package com.pepper.common.lock;

import org.springframework.util.Assert;
import redis.clients.jedis.JedisCluster;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description: 分布式锁的redis实现
 */
public class RedisDistributedLock implements DistributedLock {

    private JedisCluster jedisCluster;

    /**
     * 加锁key
     */
    private String key;
    /**
     * 锁持有时间
     */
    private long holdTimeMillis;
    /**
     * 当前锁失效时间
     */
    private long expiresAt = 0;

    public RedisDistributedLock(JedisCluster jedisCluster, String key, long holdTimeMillis) {
        Assert.isTrue(holdTimeMillis > 0, "Argument holdTimeMillis must > 0.");

        this.jedisCluster = jedisCluster;
        this.key = key;
        this.holdTimeMillis = holdTimeMillis;
    }

    @Override
    public boolean lock() {
        return this.lock(0);
    }

    @Override
    public boolean lock(long waitTimeoutMillis) {
        while (waitTimeoutMillis >= 0) {
            long expiresAt = System.currentTimeMillis() + this.holdTimeMillis + 1;
            if (this.jedisCluster.setnx(this.key, String.valueOf(expiresAt)) == 1) {
                this.expiresAt = expiresAt;
                return true;
            }

            String expectedExpiryTime = this.jedisCluster.get(this.key);
            if (expectedExpiryTime != null && System.currentTimeMillis() > Long.parseLong(expectedExpiryTime)) {
                String actualExpiryTime = this.jedisCluster.getSet(this.key, String.valueOf(expiresAt));
                // 设置逾期时间, 并和前一次获取相等才代表当前加锁成功
                if (actualExpiryTime != null && expectedExpiryTime.equals(actualExpiryTime)) {
                    this.expiresAt = expiresAt;
                    return true;
                }
            }

            waitTimeoutMillis -= 100;
            if (waitTimeoutMillis > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        }

        return false;
    }

    @Override
    public void unLock() {
        String actualExpiryTime = this.jedisCluster.get(this.key);
        // 逾期时间相等才删除
        if (actualExpiryTime != null && actualExpiryTime.equals(String.valueOf(this.expiresAt))) {
            this.jedisCluster.del(key);
            this.expiresAt = 0;
        }
    }
}
