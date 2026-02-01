package com.exe202.skillnest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.exe202.skillnest.repository")
@EntityScan(basePackages = "com.exe202.skillnest.entity")
public class SkillnestApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("✅ Default JVM timezone set to: " + TimeZone.getDefault().getID());
        SpringApplication.run(SkillnestApplication.class, args);
    }

}
