package com.mybatistemplate.adapter.impl;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.core.GeneratorIdSqlCallback;
import com.mybatistemplate.core.IdGeneratorType;
import com.mybatistemplate.util.CommonUtil;
import com.mybatistemplate.core.LastGeneratorIdSqlCallback;
import com.mybatistemplate.core.TemplateException;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by leicheng on 2016/7/12.
 */
public class DefaultTemplateAdapter extends TemplateAdapter {
    private static final Log Log = LogFactory.getLog(DefaultTemplateAdapter.class);


    @Override
    public void insert(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, GeneratorIdSqlCallback generatorIdSqlCallback) throws Exception {
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        List<ResultMapping> idResultMappings = resultMap.getIdResultMappings();
        ResultMapping idResultMap = idResultMappings.get(0);
        String idProp = idResultMap.getProperty();
        String idColumn = idResultMap.getColumn();
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        List<String> columns = new ArrayList<>();
        for (ResultMapping resultMapping : resultMappings) {
            if (!resultMapping.getFlags().contains(ResultFlag.ID)) {
                parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), resultMapping.getProperty(), resultMapping.getJavaType()).build());
                columns.add(resultMapping.getColumn());
            }
        }
        if (idGeneratorType == IdGeneratorType.MANUAL) {
            columns.add(0, idColumn);
            parameterMappings.add(0, new ParameterMapping.Builder(ms.getConfiguration(), idProp, idResultMap.getJavaType()).build());
        }
        String sql = "insert into " + table + "(";
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql += idColumn + ",";
        }
        for (String column : columns) {
            sql += column + ",";
        }
        sql = sql.replaceAll(",$", "");
        sql += ") values (";
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql += generatorIdSqlCallback.getGeneratorIdSql(table) + ",";
        }
        for (String ignored : columns) {
            sql += "?,";
        }
        sql = sql.replaceAll(",$", "");
        sql += ")";
        Log.debug(String.format("已生成insert %s", sql));
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);

    }

    @Override
    public void getById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        String id = resultMapping.getProperty();
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
        String sql = "select * from " + table + " where id = ?";
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);

        Log.debug(String.format("已生成getById %s", sql));
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);

    }

    @Override
    public void update(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        ResultMapping idResultMap = resultMap.getIdResultMappings().get(0);
        String idProp = idResultMap.getProperty();
        String idColumn = idResultMap.getColumn();
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        List<String> columns = new ArrayList<>();
        for (ResultMapping resultMapping : resultMappings) {
            if (!resultMapping.getFlags().contains(ResultFlag.ID)) {
                parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), resultMapping.getProperty(), resultMapping.getTypeHandler()).build());
                columns.add(resultMapping.getColumn());
            }
        }
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), idProp, idResultMap.getTypeHandler()).build());
        String sql = "update " + table + " set ";
        for (String column : columns) {
            sql += column + "=?,";
        }
        sql = sql.replaceAll(",$", "");
        sql += "where " + idColumn + "=?";


        Log.debug(String.format("已生成update %s", sql));
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);
    }

    @Override
    public void deleteById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        String id = resultMapping.getProperty();
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
        String sql = "delete from " + table + " where id = ?";

        Log.debug(String.format("已生成deleteById %s", sql));
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);
    }

    @Override
    public void findByExample(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        String id = resultMapping.getProperty();

        List<ResultMapping> resultMappings = resultMap.getResultMappings();

        DynamicSqlSource sqlSource;
        String sql = "select * from " + table + " ";
        List<SqlNode> rootSqlNodes = new ArrayList<>();
        rootSqlNodes.add(new StaticTextSqlNode(sql));
        MixedSqlNode rootSql = new MixedSqlNode(rootSqlNodes);

        List<SqlNode> ifSqlNodes = new ArrayList<>();
        for (ResultMapping mapping : resultMappings) {
            String ifSql = " and " + mapping.getColumn() + " = #{" + mapping.getProperty() + "} "; //and id = #{id}
            String ifStr = mapping.getProperty() + " != null";
            IfSqlNode ifSqlNode = new IfSqlNode(new MixedSqlNode(Collections.<SqlNode>singletonList(new StaticTextSqlNode(ifSql))), ifStr);
            ifSqlNodes.add(ifSqlNode);
        }

        WhereSqlNode whereSqlNode = new WhereSqlNode(ms.getConfiguration(), new MixedSqlNode(ifSqlNodes));
        rootSqlNodes.add(whereSqlNode);

        sqlSource = new DynamicSqlSource(ms.getConfiguration(), rootSql);

        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);

    }

    @Override
    public void findByMap(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        findByExample(ms, resultMap, table, entity);
    }


    @Override
    public void getLastGeneratorId(MappedStatement ms, ResultMap resultMap, String table, Class entity, LastGeneratorIdSqlCallback getLastGeneratorId) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        String sql = getLastGeneratorId.getLastGeneratorIdSql(table);
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, new ResultMap.Builder(ms.getConfiguration(), "1", resultMapping.getJavaType(), new ArrayList<ResultMapping>()).build());
    }
}
