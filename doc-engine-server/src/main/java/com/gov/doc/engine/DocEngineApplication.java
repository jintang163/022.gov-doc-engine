package com.gov.doc.engine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gov.doc.engine.mapper")
@EnableAsync
@EnableScheduling
public class DocEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocEngineApplication.class, args);
    }
}
