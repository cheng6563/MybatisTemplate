package com.mybatistemplate.util;

import com.mybatistemplate.base.BaseDao;
import com.mybatistemplate.core.TemplateException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
            Field field;
            try {
                field = aClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (Exception e) {
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
        throw new TemplateException("未找到属性 " + aClass.getName() + ":" + fieldName);
    }

    public static Pair<Class,Class> getEntityInfoByBaseDao(Class baseDao){
        Class entityClass = null;
        Class pkClass = null;
        Type[] genericInterfaces = baseDao.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                if (((ParameterizedType) genericInterface).getRawType().equals(BaseDao.class)) {
                    Type[] actualTypeArguments = ((ParameterizedType) genericInterface).getActualTypeArguments();
                    entityClass = (Class) actualTypeArguments[0];
                    pkClass = (Class) actualTypeArguments[1];
                }
            }
        }
        return new Pair<>(entityClass, pkClass);
    }
}
