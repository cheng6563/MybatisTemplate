package com.mybatistemplate.core;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.adapter.TemplateExAdapter;
import com.mybatistemplate.adapter.impl.DefaultTemplateAdapter;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class MapperHelper {
    private static final org.apache.ibatis.logging.Log Log = LogFactory.getLog(MapperHelper.class);

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
     * ID生成器的SQL的回调,当idGeneratorType=SQL时需要传入
     */
    private GeneratorIdSqlCallback generatorIdSqlCallback;

    /**
     * 获取最近生成Id的SQL的会调
     */
    private LastGeneratorIdSqlCallback lastGeneratorIdSqlCallback = new DefaultLastGeneratorIdSqlCallback();

    public MapperHelper() {
    }

    public void setIdGeneratorType(IdGeneratorType idGeneratorType) {
        this.idGeneratorType = idGeneratorType;
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

    public void setGeneratorIdSqlCallback(GeneratorIdSqlCallback generatorIdSqlCallback) {
        this.generatorIdSqlCallback = generatorIdSqlCallback;
    }

    public void setLastGeneratorIdSqlCallback(LastGeneratorIdSqlCallback lastGeneratorIdSqlCallback) {
        this.lastGeneratorIdSqlCallback = lastGeneratorIdSqlCallback;
    }


    public void setGeneratorIdSql(final String generatorIdSql) {
        this.generatorIdSqlCallback = new GeneratorIdSqlCallback() {
            @Override
            public String getGeneratorIdSql(String tableName) {
                return generatorIdSql;
            }
        };
    }

    public void setLastGeneratorIdSql(final String lastGeneratorIdSql) {
        this.lastGeneratorIdSqlCallback = new LastGeneratorIdSqlCallback() {
            @Override
            public String getLastGeneratorIdSql(String tableName) {
                return lastGeneratorIdSql;
            }
        };
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
                    try {
                        setSqlSource(ms);
                    } catch (Exception e) {
                        throw new TemplateException(e);
                    }
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
            /*if (aClass.equals(BaseDao.class) || aClass.equals(BaseDaoEx.class)) {
                return false;
            }
            Class<?>[] interfaces = aClass.getInterfaces();
            boolean isTemplateClass = false;
            for (Class<?> anInterface : interfaces) {
                if (anInterface.equals(BaseDao.class) || anInterface.equals(BaseDaoEx.class)) {
                    isTemplateClass = true;
                }
            }*/
            //if (isTemplateClass) {
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getAnnotation(TemplateMethod.class) != null) {
                    return true;
                }
            }
            //}
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setSqlSource(MappedStatement ms) throws Exception {
        Log.debug(String.format("开始初始化 %s", ms.getId()));
        String className = ms.getId().substring(0, ms.getId().lastIndexOf("."));
        String methodName = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
        ResultMap resultMap = null;
        try {
            resultMap = ms.getConfiguration().getResultMap(className + "." + defaultResultMapName);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (resultMap.getIdResultMappings().size() > 1) {
            Log.warn(String.format("%s :检测到多个主键。", className + "." + defaultResultMapName));
        }
        String tableName = ms.getConfiguration().getSqlFragments().get(className + "." + defaultTableName).getStringBody().trim();
        String versionProperty = null;
        XNode versionPropertyNode = ms.getConfiguration().getSqlFragments().get(className + "." + "_versionProperty");
        if (versionPropertyNode != null) {
            versionProperty = versionPropertyNode.getStringBody().trim();
        }
        Class<?> aClass = Class.forName(className);
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            TemplateMethod annotation = method.getAnnotation(TemplateMethod.class);
            if (method.getName().equals(methodName) && annotation != null) {
                switch (annotation.value()) {
                    case GetById:
                        templateAdapter.getById(ms, resultMap, tableName, resultMap.getType());
                        break;
                    case Insert:
                        templateAdapter.insert(ms, resultMap, tableName, resultMap.getType(), idGeneratorType, generatorIdSqlCallback);
                        break;
                    case Update:
                        if (versionProperty == null || versionProperty.trim().isEmpty()) {
                            templateAdapter.update(ms, resultMap, tableName, resultMap.getType());
                        } else {
                            templateAdapter.update(ms, resultMap, tableName, versionProperty, resultMap.getType());
                        }
                        break;
                    case DeleteById:
                        templateAdapter.deleteById(ms, resultMap, tableName, resultMap.getType());
                        break;
                    case FindByExample:
                        templateAdapter.findByExample(ms, resultMap, tableName, resultMap.getType());
                        break;
                    case FindByMap:
                        templateAdapter.findByMap(ms, resultMap, tableName, resultMap.getType());
                        break;
                    case InsertBatch:
                        if (templateExAdapter != null) {
                            templateExAdapter.insertBatch(ms, resultMap, tableName, resultMap.getType(), idGeneratorType, generatorIdSqlCallback);
                        }
                        break;
                    case UpdateBatch:
                        if (templateExAdapter != null) {
                            templateExAdapter.updateBatch(ms, resultMap, tableName, resultMap.getType());
                        }
                        break;
                    case GetLastGeneratorId:
                        templateAdapter.getLastGeneratorId(ms, resultMap, tableName, resultMap.getType(), lastGeneratorIdSqlCallback);
                        break;
                }
            }
        }
    }

}