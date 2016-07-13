package com.mybatistemplate.adapter;

import com.mybatistemplate.core.IdGeneratorType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

/**
 * Created by leicheng on 2016/7/12.
 */
public abstract class TemplateAdapter {
    abstract public void insert(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, String idGeneratorSql);

    abstract public void getById(MappedStatement ms, ResultMap resultMap, String table, Class entity);

    abstract public void update(MappedStatement ms, ResultMap resultMap, String table, Class entity);

    abstract public void deleteById(MappedStatement ms, ResultMap resultMap, String table, Class entity);

    abstract public void findByExample(MappedStatement ms, ResultMap resultMap, String table, Class entity);

    abstract public void findByMap(MappedStatement ms, ResultMap resultMap, String table, Class entity);

}
