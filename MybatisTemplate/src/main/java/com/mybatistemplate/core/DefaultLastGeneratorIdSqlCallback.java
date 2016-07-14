package com.mybatistemplate.core;

/**
 * Created by leicheng on 2016/7/14.
 */
public class DefaultLastGeneratorIdSqlCallback implements LastGeneratorIdSqlCallback {
    @Override
    public String getLastGeneratorIdSql(String tableName) {
        return "select @@IDENTITY ";
    }
}
