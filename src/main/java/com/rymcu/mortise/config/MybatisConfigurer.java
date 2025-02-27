package com.rymcu.mortise.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

import static com.rymcu.mortise.core.constant.ProjectConstant.MAPPER_PACKAGE;
import static com.rymcu.mortise.core.constant.ProjectConstant.MODEL_PACKAGE;


/**
 * Mybatis & Mapper & PageHelper 配置
 *
 * @author ronger
 */
@Configuration
@MapperScan(basePackages = MAPPER_PACKAGE)
public class MybatisConfigurer {

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTypeAliasesPackage(MODEL_PACKAGE);
        // 配置 MyBatis-Plus 插件
        factory.setPlugins(mybatisPlusInterceptor());
        //添加XML目录
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        // 配置 MyBatis-Plus 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false); // 关闭 MyBatis-Plus banner
        factory.setGlobalConfig(globalConfig);

        // MyBatis 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true); // 开启驼峰命名
        configuration.setDefaultExecutorType(ExecutorType.REUSE);
        configuration.setCacheEnabled(false);
        factory.setConfiguration(configuration);

        return factory.getObject();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

}

