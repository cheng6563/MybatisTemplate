package com.mybatistemplate.core;

import com.mybatistemplate.adapter.TemplateAdapter;
import com.mybatistemplate.adapter.TemplateExAdapter;
import com.mybatistemplate.adapter.impl.DefaultTemplateAdapter;
import com.mybatistemplate.util.CommonUtil;
import com.mybatistemplate.util.Pair;
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
     * 获取最近生成Id的SQL的回调
     */
    private LastGeneratorIdSqlCallback lastGeneratorIdSqlCallback = new DefaultLastGeneratorIdSqlCallback();

    /**
     * 版本号字段，配置此字段后UPDATE语句会自动将此字段用作乐观锁
     */
    private String versionProperty;

    /**
     * 获取ResultMap的回调
     */
    private Class<GetResultMapCallback> getResultMapCallback;

    /**
     * 获取表名的回调
     */
    private Class<GetTableNameCallback> getTableNameCallback;

    /**
     * 对没有定义ResultMap的其他方法补充ResultMap，需要定义getResultMapCallback
     */
    private boolean isSupplementResultMap;

    public MapperHelper() {
    }

    public void setSupplementResultMap(boolean supplementResultMap) {
        isSupplementResultMap = supplementResultMap;
    }

    public void setIsSupplementResultMap(boolean supplementResultMap) {
        isSupplementResultMap = supplementResultMap;
    }

    public void setGetResultMapCallback(Class<GetResultMapCallback> getResultMapCallback) {
        this.getResultMapCallback = getResultMapCallback;
    }

    public void setGetTableNameCallback(Class<GetTableNameCallback> getTableNameCallback) {
        this.getTableNameCallback = getTableNameCallback;
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

    public void setVersionProperty(String versionProperty) {
        this.versionProperty = versionProperty;
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

    /**
     * 从配置中扫描Mapper
     * @param configuration
     * @param mapperInterface
     */
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
                String className = ms.getId().substring(0, ms.getId().lastIndexOf("."));
                String methodName = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
                String resultMapId = className + "." + defaultResultMapName;
                if (ms.getId().startsWith(prefix)) {
                    if(isMapperMethod(ms.getId())) {
                        try {
                            setSqlSource(ms, className, methodName, resultMapId);
                        } catch (Exception e) {
                            throw new TemplateException(e);
                        }
                    }else if (ms.getResultMaps().isEmpty() && isSupplementResultMap && getResultMapCallback != null){
                        try {
                            ResultMap resultMap = getResultMapCallback.newInstance().getResultMap(configuration, resultMapId, Class.forName(className));
                            CommonUtil.setResultMap(ms, resultMap);
                        } catch (Exception e) {
                            throw new TemplateException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断是否为模板方法
     * @param id
     * @return
     */
    private boolean isMapperMethod(String id) {
        try {
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> aClass = Class.forName(className);
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getAnnotation(TemplateMethod.class) != null) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置模板方法
     * @param ms
     * @param className
     * @param methodName
     * @param resultMapId
     * @throws Exception
     */
    public void setSqlSource(MappedStatement ms, String className, String methodName, String resultMapId) throws Exception {
        Log.debug(String.format("开始初始化 %s", ms.getId()));
        Class<?> aClass = Class.forName(className);
        ResultMap resultMap = null;
        try {
            resultMap = ms.getConfiguration().getResultMap(resultMapId);
        } catch (IllegalArgumentException e) {
            if(getResultMapCallback != null){
                resultMap = getResultMapCallback.newInstance().getResultMap(ms.getConfiguration(), resultMapId,aClass);
            }else {
                Log.debug("未找到ResultMap: " + e.getMessage());
                return;
            }
        }
        if (resultMap.getIdResultMappings().size() > 1) {
            Log.warn(String.format("%s :检测到多个主键。", resultMapId));
        }
        String tableName;
        XNode xNode = ms.getConfiguration().getSqlFragments().get(className + "." + defaultTableName);
        if(xNode == null){
            if(getTableNameCallback != null){
                tableName = getTableNameCallback.newInstance().getTableName(aClass);
            }else {
                Log.warn("未找到tableName: " + defaultTableName);
                return;
            }
        }else{
            tableName = xNode.getStringBody().trim();
        }

        String versionProperty = null;

        try {
            XNode versionPropertyNode = ms.getConfiguration().getSqlFragments().get(className + "." + this.versionProperty);
            if (versionPropertyNode != null) {
                versionProperty = versionPropertyNode.getStringBody().trim();
            }else{
                Log.debug(String.format("已配置版本号字段[%s]，但并未从实体类[%s]的ResultMap中找到这个字段。", this.versionProperty, className));
            }
        }catch (IllegalArgumentException ignored){
        }
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