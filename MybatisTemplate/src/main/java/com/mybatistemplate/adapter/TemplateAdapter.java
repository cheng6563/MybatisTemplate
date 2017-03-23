package com.mybatistemplate.adapter;

import com.mybatistemplate.core.GeneratorIdSqlCallback;
import com.mybatistemplate.core.IdGeneratorType;
import com.mybatistemplate.core.LastGeneratorIdSqlCallback;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

/**
 * Created by leicheng on 2016/7/12.
 */
public abstract class TemplateAdapter {
    abstract public void insert(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, GeneratorIdSqlCallback generatorIdSqlCallback) throws Exception;

    abstract public void getById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception;

    abstract public void update(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception;

    abstract public void update(MappedStatement ms, ResultMap resultMap, String table,String versionProperty, Class entity) throws Exception;

    abstract public void deleteById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception;

    abstract public void findByExample(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception;

    abstract public void findByMap(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception;

    abstract public void getLastGeneratorId(MappedStatement ms, ResultMap resultMap, String table, Class entity,LastGeneratorIdSqlCallback getLastGeneratorId) throws Exception;
}
