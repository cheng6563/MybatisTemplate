package com.mybatistemplate.util;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.mybatistemplate.base.BaseDao;
import com.mybatistemplate.core.GetResultMapCallback;
import com.mybatistemplate.core.GetTableNameCallback;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 使MybatisTemplater支持使用Mybatis-Plus生成的实体类
 *
 */
@SuppressWarnings("unchecked")
public class MybatisPlusAnnotationsHandler implements GetResultMapCallback, GetTableNameCallback {

    public MybatisPlusAnnotationsHandler() {
        try {
            Class.forName("com.baomidou.mybatisplus.annotations.TableName");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("不能读取到MybatisPlus注解类", e);
        }
    }

    @Override
    public String getTableName(Class<?> aClass) {

        return aClass.getAnnotation(TableName.class).value();
    }

    @Override
    public ResultMap getResultMap(Configuration configuration, String s, Class<?> aClass) {
        Pair<Class, Class> entityInfoByBaseDao = CommonUtil.getEntityInfoByBaseDao(aClass);
        Class entityClass = entityInfoByBaseDao.getValue0();
        Class pkClass = entityInfoByBaseDao.getValue1();
        if (entityClass == null) {
            throw new RuntimeException("并非继承 com.mybatistemplate.base.BaseDao：" + aClass);
        }
        List<ResultMapping> resultMappings = new ArrayList<>();
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.getAnnotation(TableField.class) != null) {
                TableField tableField = declaredField.getAnnotation(TableField.class);
                String column = tableField.value();
                ResultMapping.Builder builder = new ResultMapping.Builder(configuration, declaredField.getName(), column, configuration.getTypeHandlerRegistry().getTypeHandler(declaredField.getType()));
                builder.javaType(declaredField.getType());
                resultMappings.add(builder.build());
            }
            if (declaredField.getAnnotation(TableId.class) != null) {
                TableId tableId = declaredField.getAnnotation(TableId.class);
                String column = tableId.value();
                ResultMapping.Builder builder = new ResultMapping.Builder(configuration, declaredField.getName(), column, configuration.getTypeHandlerRegistry().getTypeHandler(declaredField.getType()));
                builder.flags(Collections.singletonList(ResultFlag.ID));
                builder.javaType(declaredField.getType());
                builder.typeHandler(configuration.getTypeHandlerRegistry().getTypeHandler(pkClass));
                resultMappings.add(builder.build());
            }
        }
        ResultMap.Builder builder = new ResultMap.Builder(configuration, s, entityClass, resultMappings);
        return builder.build();
    }
}
