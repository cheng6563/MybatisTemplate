package com.mybatistemplate.core;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

import java.util.Objects;

public class MapperData {
    MappedStatement ms;
    ResultMap resultMap;
    Class entityClass;
    String tableName;

    public MappedStatement getMs() {
        return ms;
    }

    public ResultMap getResultMap() {
        return resultMap;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapperData that = (MapperData) o;
        return Objects.equals(ms, that.ms) &&
                Objects.equals(resultMap, that.resultMap) &&
                Objects.equals(entityClass, that.entityClass) &&
                Objects.equals(tableName, that.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ms, resultMap, entityClass, tableName);
    }
}
