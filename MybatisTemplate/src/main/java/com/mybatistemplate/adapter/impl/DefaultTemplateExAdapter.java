package com.mybatistemplate.adapter.impl;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.adapter.TemplateExAdapter;
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
import java.util.*;

/**
 * Created by leicheng on 2016/7/12.
 */
public class DefaultTemplateExAdapter extends TemplateExAdapter {
    private static final Log Log = LogFactory.getLog(DefaultTemplateExAdapter.class);

    @Override
    public void insertBatch(MappedStatement ms, ResultMap resultMap, String table, Class entity, IdGeneratorType idGeneratorType, GeneratorIdSqlCallback generatorIdSqlCallback) throws Exception {
        List<ResultMapping> idResultMappings = resultMap.getIdResultMappings();
        ResultMapping idResultMap = idResultMappings.get(0);
        String idProp = idResultMap.getProperty();
        String idColumn = idResultMap.getColumn();
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        List<String> columns = new ArrayList<>();
        Map<String, String> columnPropMap = new HashMap<>();
        Class<?> idType = null;
        for (ResultMapping resultMapping : resultMappings) {
            if (!resultMapping.getFlags().contains(ResultFlag.ID)) {
                idType = resultMapping.getJavaType();
                columns.add(resultMapping.getColumn());
                columnPropMap.put(resultMapping.getColumn(), resultMapping.getProperty());
            }
        }
        if (idGeneratorType == IdGeneratorType.MANUAL) {
            columns.add(0, idColumn);
            columnPropMap.put(idColumn, idResultMap.getProperty());
        }
        StringBuffer sql = new StringBuffer();
        sql.append("<insert>\n");
        sql.append("insert into ").append(table).append(" (");
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql.append(idColumn).append(",");
        }
        for (String column : columns) {
            sql.append(column).append(",");
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(") values ");

        //<foreach collection="ids" open="(" close=")" separator="," item="item">#{item}</foreach>
        sql.append("\n<foreach collection=\"list\" separator=\",\" item=\"item\"> ");
        sql.append("(");
        if (idGeneratorType == IdGeneratorType.SQL) {
            sql.append("(").append(generatorIdSqlCallback.getGeneratorIdSql(table)).append(")").append(",");
        }
        for (String column : columns) {
            sql.append("#{item.").append(columnPropMap.get(column)).append("},");
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(")");
        sql.append(" \n</foreach>");


        sql.append("\n</insert>");


        Log.debug(String.format("已生成insert %s", sql));

        if (idGeneratorType == IdGeneratorType.EMPTY) {
            CommonUtil.setFieldValue(ms, "keyGenerator", new Jdbc3KeyGenerator());
            CommonUtil.setFieldValue(ms, "keyProperties", new String[]{idProp});
            CommonUtil.setFieldValue(ms, "keyColumns", new String[]{idColumn});
        }

        Configuration configuration = ms.getConfiguration();
        XPathParser xPathParser = new XPathParser(sql.toString());
        XNode xNode = xPathParser.evalNode("/insert");
        SqlSource sqlSource = new XMLScriptBuilder(configuration, xNode).parseScriptNode();

        CommonUtil.setSqlSource(ms, sqlSource);
        CommonUtil.setResultMap(ms, resultMap);
    }

}
