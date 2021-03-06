package com.mybatistemplate.adapter.impl;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.core.GeneratorIdSqlCallback;
import com.mybatistemplate.core.IdGeneratorType;
import com.mybatistemplate.core.LastGeneratorIdSqlCallback;
import com.mybatistemplate.core.MapperHelper;
import com.mybatistemplate.util.CommonUtil;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        Class<?> idType = null;
        for (ResultMapping resultMapping : resultMappings) {
            if (!resultMapping.getFlags().contains(ResultFlag.ID)) {
                idType = resultMapping.getJavaType();
                parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), resultMapping.getProperty(), idType).build());
                columns.add(resultMapping.getColumn());
            }
        }
        if (idGeneratorType == IdGeneratorType.MANUAL) {
            columns.add(0, idColumn);
            parameterMappings.add(0, new ParameterMapping.Builder(ms.getConfiguration(), idProp, idResultMap.getJavaType()).build());
        }
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(table).append("(");
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql.append(idColumn).append(",");
        }
        for (String column : columns) {
            sql.append(column).append(",");
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(") values (");
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql.append("(").append(generatorIdSqlCallback.getGeneratorIdSql(table)).append(")").append(",");
        }
        for (String ignored : columns) {
            sql.append("?,");
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(")");
        Log.debug(String.format("已生成insert %s", sql));

        if (idGeneratorType == IdGeneratorType.EMPTY) {
            CommonUtil.setFieldValue(ms, "keyGenerator", new Jdbc3KeyGenerator());
            CommonUtil.setFieldValue(ms, "keyProperties", new String[]{idProp});
            CommonUtil.setFieldValue(ms, "keyColumns", new String[]{idColumn});
        }
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql.toString(), parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);

    }

    @Override
    public void getById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        String id = resultMapping.getProperty();
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
        String sql = "select * from " + table + " where  " + resultMapping.getColumn() + "  = ?";
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, parameterMappings);

        Log.debug(String.format("已生成getById %s", sql));
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);

    }

    @Override
    public void update(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        update(ms, resultMap, table, null, entity);
    }

    @Override
    public void update(MappedStatement ms, ResultMap resultMap, String table, String versionProperty, Class entity) throws Exception {
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        ResultMapping idResultMap = resultMap.getIdResultMappings().get(0);
        String idProp = idResultMap.getProperty();
        String idColumn = idResultMap.getColumn();
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        List<String> columns = new ArrayList<>();
        String versionColumn = null;
        ParameterMapping versionParam = null;
        for (ResultMapping resultMapping : resultMappings) {
            if (!resultMapping.getFlags().contains(ResultFlag.ID)) {
                if (versionProperty != null && versionProperty.equals(resultMapping.getProperty())) {
                    versionParam = new ParameterMapping.Builder(ms.getConfiguration(), resultMapping.getProperty(), resultMapping.getTypeHandler()).build();
                    versionColumn = resultMapping.getColumn();
                } else {
                    parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), resultMapping.getProperty(), resultMapping.getTypeHandler()).build());
                    columns.add(resultMapping.getColumn());
                }
            }
        }
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), idProp, idResultMap.getTypeHandler()).build());
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(table).append(" set ");
        for (String column : columns) {
            sql.append(column).append("=?,");
        }
        if (versionColumn != null) {
            sql.append(versionColumn).append("=").append(versionColumn).append("+1,");  //v=v+1
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(" where ").append(idColumn).append("=?");
        if (versionColumn != null) {
            sql.append(" and ").append(versionColumn).append(" = ?");
            parameterMappings.add(versionParam);
        }

        Log.debug(String.format("已生成update %s", sql));
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql.toString(), parameterMappings);
        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);
    }

    @Override
    public void deleteById(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        ResultMapping resultMapping = resultMap.getIdResultMappings().get(0);
        String id = resultMapping.getProperty();
        ArrayList<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), id, resultMapping.getJavaType()).build());
        String sql = "delete from " + table + " where " + resultMapping.getColumn() + " = ?";

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

    @Override
    public void findByFindWrapper(MappedStatement ms, ResultMap resultMap, String table, Class entity) throws Exception {
        String xml;
        try (InputStream resourceAsStream = MapperHelper.class.getResourceAsStream("FindWrapperMapper.xml")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int ch; (ch = resourceAsStream.read()) != -1; ) {
                buffer.write(ch);
            }
            xml = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }

        xml = xml.replace("@{columns}", "*");
        xml = xml.replace("@{tableName}", table);
        Configuration configuration = ms.getConfiguration();
        XPathParser xPathParser = new XPathParser(xml);
        XNode xNode = xPathParser.evalNode("/mapper/select");
        SqlSource sqlSource = new XMLScriptBuilder(configuration, xNode).parseScriptNode();

        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);
    }

}
