package com.mybatistemplate.test;

import com.mybatistemplate.core.MapperHelper;
import com.mybatistemplate.core.SqlSessionFactoryBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

/**
 * Created by leicheng on 2016/7/12.
 */
public class MybatisHelper {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader("mybatis-java.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().setMapperHelper(new MapperHelper()).build(reader);
            reader.close();

            //创建一个MapperHelper
            //MapperHelper mapperHelper = new MapperHelper();
            //配置完成后，执行下面的操作
            //mapperHelper.processConfiguration(sqlSessionFactory.getConfiguration());
            //创建数据库
            SqlSession session = null;
            try {
                session = sqlSessionFactory.openSession();
                //OK - mapperHelper的任务已经完成，可以不管了

            } finally {
                if (session != null) {
                    session.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Session
     * @return
     */
    public static SqlSession getSqlSession(){
        return sqlSessionFactory.openSession();
    }
}
