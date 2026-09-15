package com.ccdd.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CCDDesigner 2.0 模块化单体平台主启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.ccdd"})
@EnableScheduling
@EnableAsync
public class CCDDApplication {

    public static void main(String[] args) {
        SpringApplication.run(CCDDApplication.class, args);
    }
}
