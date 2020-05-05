package com.example.sec.kill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/5
 * @Description:
 */
@SpringBootApplication
@MapperScan("com.example.sec.kill.dal.dao")
public class SecKillApplicationStarter {
    public static void main(String[] args) {
        SpringApplication.run(SecKillApplicationStarter.class, args);
    }
}
