package com.trade.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.trade.platform.module.*.mapper")
public class TradePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradePlatformApplication.class, args);
    }
}
