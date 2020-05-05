package com.example.sec.kill;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/5
 * @Description:
 */
public class SecKillApplicationContextAware implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("====测试====");
    }
}
