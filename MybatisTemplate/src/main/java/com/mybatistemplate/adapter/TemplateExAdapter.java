package com.mybatistemplate.adapter;

import com.mybatistemplate.core.GeneratorIdSqlCallback;
import com.mybatistemplate.core.IdGeneratorType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

/**
 * Created by leicheng on 2016/7/12.
 */
public abstract class TemplateExAdapter {
    public void insertBatch(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, GeneratorIdSqlCallback generatorIdSqlCallback) throws Exception{

    }

    public void updateBatch(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception{

    }

}
