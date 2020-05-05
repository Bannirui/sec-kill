package com.example.sec.kill;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/5
 * @Description:
 */
public class SecKillApplicationInit implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("----项目初始化----");
    }
}
