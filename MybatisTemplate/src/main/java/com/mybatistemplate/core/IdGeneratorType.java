package com.mybatistemplate.core;

/**
 * Created by leicheng on 2016/7/13.
 */
public enum IdGeneratorType {
    MANUAL, EMPTY, SQL
    //手动插入ID, 不插入ID(数据库自增), 使用一段SQL取得ID
}
