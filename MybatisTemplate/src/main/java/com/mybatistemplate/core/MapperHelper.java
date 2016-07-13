package com.mybatistemplate.core;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.adapter.TemplateExAdapter;
import com.mybatistemplate.adapter.impl.DefaultTemplateAdapter;
import com.mybatistemplate.base.BaseDao;
import com.mybatistemplate.base.BaseDaoEx;
import com.mybatistemplate.base.TemplateMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class MapperHelper {
    /**
     * 默认ResultMap名称
     */
    private String defaultResultMapName = "_defaultResultMap";
    /**
     * 默认表名称的SqlId
     */
    private String defaultTableName = "_tableName";
    private TemplateAdapter templateAdapter = new DefaultTemplateAdapter();
    private TemplateExAdapter templateExAdapter;
    /**
     * Id生成方式
     */
    private IdGeneratorType idGeneratorType = IdGeneratorType.EMPTY;

    /**
     * Id生成器Sql
     */
    private String idGeneratorSql;

    public MapperHelper() {
    }

    public void setIdGeneratorType(IdGeneratorType idGeneratorType) {
        this.idGeneratorType = idGeneratorType;
    }

    public void setIdGeneratorSql(String idGeneratorSql) {
        this.idGeneratorSql = idGeneratorSql;
    }

    public void setIdGeneratorType(String idGeneratorType) {
        this.idGeneratorType = IdGeneratorType.valueOf(idGeneratorType);
    }

    public void setDefaultResultMapName(String defaultResultMapName) {
        this.defaultResultMapName = defaultResultMapName;
    }

    public void setDefaultTableName(String defaultTableName) {
        this.defaultTableName = defaultTableName;
    }

    public void setTemplateAdapter(TemplateAdapter templateAdapter) {
        this.templateAdapter = templateAdapter;
    }

    public void setTemplateExAdapter(TemplateExAdapter templateExAdapter) {
        this.templateExAdapter = templateExAdapter;
    }

    public void processConfiguration(Configuration configuration) {
        processConfiguration(configuration, null);
    }

    public void processConfiguration(Configuration configuration, Class<?> mapperInterface) {
        String prefix;
        if (mapperInterface != null) {
            prefix = mapperInterface.getCanonicalName();
        } else {
            prefix = "";
        }
        for (Object object : new ArrayList<Object>(configuration.getMappedStatements())) {
            if (object instanceof MappedStatement) {
                MappedStatement ms = (MappedStatement) object;
                if (ms.getId().startsWith(prefix) && isMapperMethod(ms.getId())) {
                    setSqlSource(ms);
                } else {
                    int i = 0;
                }
            }
        }
    }

    private boolean isMapperMethod(String id) {
        try {
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> aClass = Class.forName(className);
            if (aClass.equals(BaseDao.class) || aClass.equals(BaseDaoEx.class)) {
                return false;
            }
            Class<?>[] interfaces = aClass.getInterfaces();
            boolean isTemplateClass = false;
            for (Class<?> anInterface : interfaces) {
                if (anInterface.equals(BaseDao.class) || anInterface.equals(BaseDaoEx.class)) {
                    isTemplateClass = true;
                }
            }
            if (isTemplateClass) {
                Method[] methods = aClass.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName) && method.getAnnotation(TemplateMethod.class) != null) {
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setSqlSource(MappedStatement ms) {
        String className = ms.getId().substring(0, ms.getId().lastIndexOf("."));
        String methodName = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
        ResultMap resultMap = ms.getConfiguration().getResultMap(className + "." + defaultResultMapName);
        String tableName = ms.getConfiguration().getSqlFragments().get(className + "." + defaultTableName).getStringBody().trim();
        switch (methodName) {
            case "getById":
                templateAdapter.getById(ms, resultMap, tableName, resultMap.getType());
                break;
            case "insert":
                templateAdapter.insert(ms, resultMap, tableName, resultMap.getType(), idGeneratorType, idGeneratorSql);
                break;
            case "update":
                templateAdapter.update(ms, resultMap, tableName, resultMap.getType());
                break;
            case "deleteById":
                templateAdapter.deleteById(ms, resultMap, tableName, resultMap.getType());
                break;
            case "findByExample":
                templateAdapter.findByExample(ms, resultMap, tableName, resultMap.getType());
                break;
            case "findByMap":
                templateAdapter.findByMap(ms, resultMap, tableName, resultMap.getType());
                break;
            case "insertBatch":
                if (templateExAdapter != null) {
                    templateExAdapter.insertBatch(ms, resultMap, tableName, resultMap.getType(), idGeneratorType, idGeneratorSql);
                }
                break;
            case "updateBatch":
                if (templateExAdapter != null) {
                    templateExAdapter.updateBatch(ms, resultMap, tableName, resultMap.getType());
                }
            case "getLastGeneratorId":
                if (templateExAdapter != null) {
                    templateExAdapter.getLastGeneratorId(ms, resultMap, tableName, resultMap.getType());
                }
                break;
        }

    }

}