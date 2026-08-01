package com.lmserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类 — LM-Server 应用入口，启用定时任务调度。
 */
@SpringBootApplication
@EnableScheduling
public class LmServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmServerApplication.class, args);
    }
}
