package com.tourism.saas.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@MapperScan("com.tourism.saas.user.mapper")
@ComponentScan(basePackages = {"com.tourism.saas.user", "com.tourism.saas.common"})
@EnableTransactionManagement
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("====== 用户服务启动成功 ======");
    }
}