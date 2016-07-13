package com.mybatistemplate.core;

import com.mybatistemplate.util.CommonUtil;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

/**
 * Created by leicheng on 2016/7/13.
 */
public class SqlSessionFactoryBuilder extends org.apache.ibatis.session.SqlSessionFactoryBuilder {

    private MapperHelper mapperHelper;

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        SqlSessionFactory build = super.build(inputStream, environment, properties);
        mapperHelper.processConfiguration(build.getConfiguration());
        injectMapperRegistry(build.getConfiguration());
        return build;
    }

    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        SqlSessionFactory build = super.build(reader, environment, properties);
        mapperHelper.processConfiguration(build.getConfiguration());
        injectMapperRegistry(build.getConfiguration());
        return build;
    }

    public SqlSessionFactory build(Configuration config) {
        SqlSessionFactory build = super.build(config);
        mapperHelper.processConfiguration(build.getConfiguration());
        injectMapperRegistry(build.getConfiguration());
        return build;
    }

    public SqlSessionFactoryBuilder setMapperHelper(MapperHelper mapperHelper) {
        this.mapperHelper = mapperHelper;
        return this;
    }

    /**
     * 注入MapperRegistry的代理，因为Spring会在初始化完成之后在addMapper，所以要在addMapper时再次初始化。
     * @param configuration
     */
    private void injectMapperRegistry(Configuration configuration){
        MapperRegistryProxy mapperRegistryProxy=new MapperRegistryProxy(configuration);
        mapperRegistryProxy.setMapperHelper(mapperHelper);
        MapperRegistry rawMapperRegistry = configuration.getMapperRegistry();
        Map knownMappers = (Map) CommonUtil.getFieldValue(rawMapperRegistry, "knownMappers");
        Map knownMappersNew = (Map) CommonUtil.getFieldValue(mapperRegistryProxy, "knownMappers");
        //noinspection unchecked,ConstantConditions
        knownMappersNew.putAll(knownMappers);
        CommonUtil.setFieldValue(configuration,"mapperRegistry",mapperRegistryProxy);
    }
}
