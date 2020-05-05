package com.example.sec.kill.service;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description:
 */
public interface AccessLimitService {

    /**
     * 尝试获取限速令牌
     * @return
     */
    boolean tryAcquireSecKill();
}
