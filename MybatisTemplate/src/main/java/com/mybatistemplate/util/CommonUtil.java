package com.mybatistemplate.util;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leicheng on 2016/7/12.
 */
public class CommonUtil {
    public static void setSqlSource(MappedStatement ms, SqlSource sqlSource) throws NoSuchFieldException, IllegalAccessException {
        Field sqlSourceField = ms.getClass().getDeclaredField("sqlSource");
        sqlSourceField.setAccessible(true);
        sqlSourceField.set(ms, sqlSource);
    }

    public static void setResultMap(MappedStatement ms, ResultMap resultMap) throws NoSuchFieldException, IllegalAccessException {
        Field resultMapsField = ms.getClass().getDeclaredField("resultMaps");
        resultMapsField.setAccessible(true);
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(resultMap);
        resultMapsField.set(ms, resultMaps);
    }

    public static Object getFieldValue(Object object, String fieldName) {
        Class<?> aClass = object.getClass();
        for (; !aClass.equals(Object.class); aClass = aClass.getSuperclass()) {
            Field field = null;
            try {
                field = aClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (Exception ignore) {
            }

        }
        return null;
    }


    public static void setFieldValue(Object object, String fieldName, Object value) {
        Class<?> aClass = object.getClass();
        for (; !aClass.equals(Object.class); aClass = aClass.getSuperclass()) {
            Field field = null;
            try {
                field = aClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
                return;
            } catch (Exception ignore) {
            }
        }
        //throw new RuntimeException("未找到属性 " + aClass.getName() + ":" + fieldName);
    }
}
