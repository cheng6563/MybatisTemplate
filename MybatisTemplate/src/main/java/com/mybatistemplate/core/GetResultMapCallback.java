package com.mybatistemplate.core;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;

public interface GetResultMapCallback {
    ResultMap getResultMap(Configuration configuration, String resultMapId, Class<?> clazz);
}
