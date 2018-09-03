package com.mybatistemplate.util;

import com.mybatistemplate.base.BaseDao;
import com.mybatistemplate.core.TemplateException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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


    /**
     * 通过硬编码lambda取出方法名
     */
    public static <T> Method getMethod(Class<T> clazz, Getter<T> function) {
        if (clazz.isInterface()) {
            final Method[] methodBox = new Method[1];
            Object tmpObject = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    methodBox[0] = method;
                    if (method.getReturnType().isPrimitive()) {
                        //利用Array取出基本类型的默认值
                        return Array.get(Array.newInstance(method.getReturnType(), 1), 0);
                    } else {
                        return null;
                    }
                }
            });
            function.get((T) tmpObject);
            return methodBox[0];
        } else {
            final Method[] methodBox = new Method[1];
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    methodBox[0] = method;
                    if (method.getReturnType().isPrimitive()) {
                        //利用Array取出基本类型的默认值
                        return Array.get(Array.newInstance(method.getReturnType(), 1), 0);
                    } else {
                        return null;
                    }
                }
            });
            T tmpObject = (T) enhancer.create();
            function.get(tmpObject);
            return methodBox[0];
        }
    }

    public static <T> Field getFieldByGetter(Class<T> clazz, Getter<T> function) {
        return getGetterMethodField(getMethod(clazz, function));
    }

    public static Field getGetterMethodField(Method method) {
        try {
            Class<?> declaringClass = method.getDeclaringClass();
            for (PropertyDescriptor pd : Introspector.getBeanInfo(declaringClass).getPropertyDescriptors()) {
                if (pd.getReadMethod().equals(method)) {
                    return declaringClass.getDeclaredField(pd.getName());
                }
            }
        } catch (IntrospectionException | NoSuchFieldException e) {
            throw new UnsupportedOperationException("操作失败", e);
        }
        return null;
    }


    public static Type[] findClassInterfaceGenericType(Class targetClass, Class superInterfaceType, List<Class> tempList) {
        if (tempList.contains(targetClass)) {
            return null;
        }
        Type[] genericInterfaces = targetClass.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                tempList.add(targetClass);
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                if (superInterfaceType.equals(parameterizedType.getRawType())) {
                    return parameterizedType.getActualTypeArguments();
                }
                return findClassInterfaceGenericType((Class) ((ParameterizedType) genericInterface).getRawType(), superInterfaceType, tempList);
            }
        }
        return null;
    }
}
