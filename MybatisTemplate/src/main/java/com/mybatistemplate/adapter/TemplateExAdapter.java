package com.mybatistemplate.adapter;

import com.mybatistemplate.base.TemplateMethod;
import com.mybatistemplate.core.IdGeneratorType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

/**
 * Created by leicheng on 2016/7/12.
 */
public abstract class TemplateExAdapter {
    public void insertBatch(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, String idGeneratorSql){

    }

    public void updateBatch(MappedStatement ms, ResultMap resultMap, String table, Class entity){

    }

    public void getLastGeneratorId(MappedStatement ms, ResultMap resultMap, String table, Class entity){

    }
}
