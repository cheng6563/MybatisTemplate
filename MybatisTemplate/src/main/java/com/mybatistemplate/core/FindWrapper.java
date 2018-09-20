package com.mybatistemplate.core;

import com.mybatistemplate.util.CommonUtil;
import com.mybatistemplate.util.Getter;
import com.mybatistemplate.util.Pair;
import org.apache.ibatis.mapping.ResultMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class FindWrapper<T> {
    private MapperHelper mapperHelper;
    private List<Condition> conditions = new ArrayList<>();
    private String orderProp;
    private String orderColumn;
    private boolean orderAsc = true;
    private Class<T> entityClass;

    public FindWrapper(MapperHelper mapperHelper) {
        this.mapperHelper = mapperHelper;
    }

    public FindWrapper() {
        this.mapperHelper = MapperHelper.MAPPER_HELPERS.get(0);
    }

    public FindWrapper(Class<T> entityClass, MapperHelper mapperHelper) {
        this.mapperHelper = mapperHelper;
        this.entityClass = entityClass;
    }

    public FindWrapper(Class<T> entityClass) {
        this.mapperHelper = MapperHelper.MAPPER_HELPERS.get(0);
        this.entityClass = entityClass;
    }


    public FindWrapper<T> addCondition(String field, ConditionSymbol symbol, Object value) {
        value = checkValueType(symbol, value);
        Condition e = new Condition();
        e.propName = field;
        e.symbol = symbol;
        e.symbolString = symbol.name();
        e.value = value;

        e.column = getColumn(field);

        conditions.add(e);
        return this;
    }

    private String getColumn(String field) {
        if(entityClass == null){
            throw new RuntimeException("使用String类型属性时必须在构造时传入实体类Class");
        }
        String column = null;
        for (ResultMapping resultMapping : mapperHelper.getMapperDataMap().get(entityClass).getResultMap().getResultMappings()) {
            if (resultMapping.getProperty().equals(field)) {
                column = resultMapping.getColumn();
            }
        }
        if (column == null) {
            throw new IllegalArgumentException("无效的属性: " + entityClass + "#" + field);
        }
        return column;
    }

    private Object checkValueType(ConditionSymbol symbol, Object value) {
        switch (symbol) {
            case BETWEEN:
                if(value == null){
                    throw new IllegalArgumentException("BETWEEN的参数不可为null");
                }
                if (value instanceof Pair) {
                    return value;
                }
                if (value.getClass().isArray()) {
                    Object[] array = (Object[]) value;
                    return new Pair<>(array[0], array[1]);
                }
                if (value instanceof List) {
                    return new Pair<>(((List) value).get(0), ((List) value).get(1));
                }
                if (value instanceof Map.Entry) {
                    return new Pair<>(((Map.Entry) value).getKey(), ((Map.Entry) value).getValue());
                }
                Object value0 = CommonUtil.getFieldValue(value, "value0");
                Object value1 = CommonUtil.getFieldValue(value, "value1");
                Object key = CommonUtil.getFieldValue(value, "key");
                Object _value = CommonUtil.getFieldValue(value, "value");
                if (value0 != null && value1 != null) {
                    return new Pair<>(value0, value1);
                }
                if (key != null && _value != null) {
                    return new Pair<>(key, _value);
                }
                throw new IllegalArgumentException("BETWEEN的参数类型必须为 com.mybatistemplate.util.Pair 的同结构类型");
            case IN:
                if(value == null){
                    return Collections.emptyList();
                }
                if (value.getClass().isArray()) {
                    return Arrays.asList((Object[]) value);
                }
                if (value instanceof Collection) {
                    return value;
                }
                throw new IllegalArgumentException("BETWEEN的参数类型必须为集合或数组");
            case IS_NULL:
            case IS_NOT_NULL:
                return null;
        }
        return value;
    }


    public FindWrapper<T> addCondition(Getter<T> prop, ConditionSymbol symbol, Object value) {
        makeEntityType(prop);
        Field propField = CommonUtil.getFieldByGetter(entityClass, prop);
        addCondition(propField.getName(), symbol, value);
        return this;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public FindWrapper<T> setConditions(List<Condition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public String getOrderProp() {
        return orderProp;
    }

    public FindWrapper<T> setOrderProp(String orderProp, boolean asc) {
        if(entityClass == null){
            throw new RuntimeException("使用String类型属性时必须在构造时传入实体类Class");
        }
        this.orderProp = orderProp;
        this.orderColumn = getColumn(orderProp);
        this.orderAsc = asc;
        return this;
    }

    public FindWrapper<T> setOrderProp(Getter<T> orderProp, boolean asc) {
        makeEntityType(orderProp);
        Field propField = CommonUtil.getFieldByGetter(entityClass, orderProp);
        this.orderProp = propField.getName();
        setOrderProp(this.orderProp, asc);
        return this;
    }

    private void makeEntityType(Getter<T> orderProp) {
        if(this.entityClass != null){
            return;
        }
        Type[] classInterfaceGenericType = CommonUtil.findClassInterfaceGenericType(orderProp.getClass(), Getter.class, new ArrayList<Class>());
        if (classInterfaceGenericType == null || classInterfaceGenericType.length == 0) {
            throw new IllegalArgumentException("不能从Getter中找到泛型类型");
        }
        Type type = classInterfaceGenericType[0];
        this.entityClass = (Class<T>) type;
    }

    public boolean isOrderAsc() {
        return orderAsc;
    }

    public String getOrderColumn() {
        return orderColumn;
    }
}
