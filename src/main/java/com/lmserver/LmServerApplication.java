package com.lmserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类 — 应用入口，@SpringBootApplication
 */

@SpringBootApplication
public class LmServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmServerApplication.class, args);
    }
}
