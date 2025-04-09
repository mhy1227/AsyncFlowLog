package com.asyncflow.log;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

/**
 * 测试应用类，专用于测试环境
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    MybatisAutoConfiguration.class
})
@Profile("test")
@ComponentScan(basePackages = "com.asyncflow.log", 
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.asyncflow\\.log\\.config\\.(?!TestConfig).*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.asyncflow\\.log\\.mapper\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.asyncflow\\.log\\.controller\\.(?!TestOperationLogController).*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.asyncflow\\.log\\.service\\.impl\\..*")
    }
)
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
} 