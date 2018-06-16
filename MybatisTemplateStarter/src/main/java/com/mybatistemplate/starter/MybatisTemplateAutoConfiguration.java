package com.mybatistemplate.starter;

import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import com.mybatistemplate.core.MapperRegistryProxy;
import com.mybatistemplate.core.SqlSessionFactoryBuilder;
import com.mybatistemplate.util.CommonUtil;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@EnableConfigurationProperties(MapperHelper.class)
@AutoConfigureAfter(SqlSessionFactory.class)
public class MybatisTemplateAutoConfiguration {

    @Autowired
    private MapperHelper mapperHelper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void addMapperHelper() {
        if(!(sqlSessionFactory instanceof DefaultSqlSessionFactory)){
            System.err.println("Invalid sqlSessionFactory");
            return;
        }
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        mapperHelper.processConfiguration(configuration);

        //注入MapperRegistry的代理，因为Spring会在初始化完成之后在addMapper，所以要在addMapper时再次初始化。
        MapperRegistryProxy mapperRegistryProxy=new MapperRegistryProxy(configuration);
        mapperRegistryProxy.setMapperHelper(mapperHelper);
        MapperRegistry rawMapperRegistry = configuration.getMapperRegistry();
        Map knownMappers = (Map) CommonUtil.getFieldValue(rawMapperRegistry, "knownMappers");
        Map knownMappersNew = (Map) CommonUtil.getFieldValue(mapperRegistryProxy, "knownMappers");
        //noinspection unchecked,ConstantConditions
        knownMappersNew.putAll(knownMappers);
        CommonUtil.setFieldValue(configuration,"mapperRegistry", mapperRegistryProxy);
    }
}
