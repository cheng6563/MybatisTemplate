package com.mybatistemplate.core;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;

/**
 * Created by leicheng on 2016/7/13.
 *
 */
class MapperRegistryProxy extends MapperRegistry {
    private MapperHelper mapperHelper;
    private Configuration config;
    MapperRegistryProxy(Configuration config) {
        super(config);
        this.config=config;
    }

    public void setMapperHelper(MapperHelper mapperHelper) {
        this.mapperHelper = mapperHelper;
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        super.addMapper(type);
        mapperHelper.processConfiguration(config,type);
    }
}
