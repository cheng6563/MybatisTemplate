package com.mybatistemplate.core;

public class Condition {
    String propName;
    ConditionSymbol symbol;
    String symbolString;
    String column;
    Object value;

    public String getPropName() {
        return propName;
    }

    public ConditionSymbol getSymbol() {
        return symbol;
    }

    public String getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }
}
