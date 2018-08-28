package com.pepper.common.lock;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:分布式锁
 */
public interface DistributedLock {
    /**
     * 加锁, 立即返回
     * @return true 成功
     */
    boolean lock();

    /**
     * 加锁
     *
     * @param waitTimeoutMillis 等待时间,毫秒
     * @return true 成功
     */
    boolean lock(long waitTimeoutMillis);

    /**
     * 解锁
     */
    void unLock();
}
