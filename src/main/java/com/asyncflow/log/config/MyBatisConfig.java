package com.asyncflow.log.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.FileNotFoundException;

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
        try {
            factoryBean.setConfigLocation(
                new PathMatchingResourcePatternResolver()
                    .getResource("classpath:mybatis-config.xml")
            );
        } catch (Exception e) {
            LoggerFactory.getLogger(MyBatisConfig.class)
                .warn("无法加载mybatis-config.xml配置文件: {}", e.getMessage());
        }
        
        // 设置 Mapper XML 文件路径
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml");
            
            if (resources != null && resources.length > 0) {
                factoryBean.setMapperLocations(resources);
                LoggerFactory.getLogger(MyBatisConfig.class)
                    .info("成功加载{}个Mapper XML文件", resources.length);
            } else {
                LoggerFactory.getLogger(MyBatisConfig.class)
                    .warn("未找到Mapper XML文件，将使用注解方式的Mapper");
            }
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(MyBatisConfig.class)
                .warn("Mapper目录不存在，将使用注解方式的Mapper: {}", e.getMessage());
        } catch (Exception e) {
            LoggerFactory.getLogger(MyBatisConfig.class)
                .error("加载Mapper XML文件时发生错误: {}", e.getMessage());
        }
        
        // 设置别名包
        factoryBean.setTypeAliasesPackage("com.asyncflow.log.model.entity");
        
        return factoryBean.getObject();
    }
} 