package com.example.sec.kill.service.impl;

import com.example.sec.kill.service.AccessLimitService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description:
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AccessLimitServiceImpl implements AccessLimitService {

    @Value("${sec-kill.rate-limit}")
    private Long rateLimit;

    /**
     * 尝试获取限速令牌
     * @return
     */
    @Override
    public boolean tryAcquireSecKill() {
        // 每秒发放rateLimit个令牌 拿到令牌的请求才可以进入秒杀过程
        RateLimiter rateLimiter = RateLimiter.create(rateLimit);
        return rateLimiter.tryAcquire();
    }
}
