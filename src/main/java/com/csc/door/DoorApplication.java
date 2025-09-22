package com.csc.door;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
public class DoorApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(DoorApplication.class, args);
        } catch (Exception e) {
            log.error("启动错误:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.info("门禁服务启动成功!!!");
    }

}

