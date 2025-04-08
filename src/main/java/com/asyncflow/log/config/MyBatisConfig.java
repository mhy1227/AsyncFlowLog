package com.asyncflow.log.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.asyncflow.log.mapper")
public class MyBatisConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置 MyBatis 配置文件路径
        factoryBean.setConfigLocation(
            new PathMatchingResourcePatternResolver()
                .getResource("classpath:mybatis-config.xml")
        );
        
        // 设置 Mapper XML 文件路径
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml")
        );
        
        // 设置别名包
        factoryBean.setTypeAliasesPackage("com.asyncflow.log.model.entity");
        
        return factoryBean.getObject();
    }
} 