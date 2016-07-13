package com.mybatistemplate.adapter.impl;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.core.IdGeneratorType;
import com.mybatistemplate.util.CommonUtil;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by leicheng on 2016/7/12.
 */
public class DefaultTemplateAdapter extends TemplateAdapter {
    private static final Log log = LogFactory.getLog(DefaultTemplateAdapter.class);


    @Override
    public void insert(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, String idGeneratorSql) {
        try {
            ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
            ResultMapping idResultMap = resultMap.getIdResultMappings().get(0);
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
            switch (idGeneratorType) {
                case EMPTY:
                    break;
                case MANUAL:
                    columns.add(0, idColumn);
                    parameterMappings.add(0, new ParameterMapping.Builder(ms.getConfiguration(), idProp, idResultMap.getJavaType()).build());
                    break;
            }
            String sql = "insert into " + table + "(";
            for (String column : columns) {
                sql += column + ",";
            }
            sql = sql.replaceAll(",$", "");
            sql += ") values (";
            if (idGeneratorType == IdGeneratorType.SQL) {
                sql += idGeneratorSql + ",";
            }
            for (String column : columns) {
                sql += "?,";
            }
            sql = sql.replaceAll(",$", "");
            sql += ")";

            SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
            CommonUtil.setSqlSource(ms, sqlSource);
            CommonUtil.setResultMap(ms, resultMap);
        } catch (Exception e) {
            log.error("insert出错", e);
        }
    }

    @Override
    public void getById(MappedStatement ms, ResultMap resultMap, String table, Class entity) {
        try {
            ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
            String id = resultMapping.getProperty();
            ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
            parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
            String sql = "select * from " + table + " where id = ?";
            SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
            CommonUtil.setSqlSource(ms, sqlSource);
            CommonUtil.setResultMap(ms, resultMap);
        } catch (Exception e) {
            log.error("getById出错", e);
        }
    }

    @Override
    public void update(MappedStatement ms, ResultMap resultMap, String table, Class entity) {
        try {
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

            SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);
            CommonUtil.setSqlSource(ms, sqlSource);
            CommonUtil.setResultMap(ms, resultMap);
        } catch (Exception e) {
            log.error("update出错", e);
        }
    }

    @Override
    public void deleteById(MappedStatement ms, ResultMap resultMap, String table, Class entity) {
        try {
            ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
            String id = resultMapping.getProperty();
            ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
            parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
            SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), "delete from " + table + " where id = ?", parameterMappings);
            CommonUtil.setSqlSource(ms, sqlSource);
            CommonUtil.setResultMap(ms, resultMap);
        } catch (Exception e) {
            log.error("deleteById出错", e);
        }
    }

    @Override
    public void findByExample(MappedStatement ms, ResultMap resultMap, String table, Class entity) {
        try {
            ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
            String id = resultMapping.getProperty();
            ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
            parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());

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
        } catch (Exception e) {
            log.error("findByMap出错", e);
        }
    }

    @Override
    public void findByMap(MappedStatement ms, ResultMap resultMap, String table, Class entity) {
        findByExample(ms, resultMap, table, entity);
    }
}
