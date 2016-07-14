package com.mybatistemplate.springdemo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.List;

/**
 * Created by leicheng on 2016/7/13.
 */
public class Test1OnSpring {

    private ApplicationContext applicationContext;
    private TestService testService;

    @Before
    public void init(){
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        testService = applicationContext.getBean(TestService.class);
    }

    @Test
    public void test1(){
        Country rs = testService.getById(1);
        System.out.println(rs);
    }

    @Test
    public void test2(){
        List<Country> rs = testService.testSelect(new HashMap<String, Object>(),1,2);
        System.out.println(rs);
        for (Country r : rs) {
            System.out.println(r);
        }
    }


    @Test
    public void testGetLastGeneratorId(){
        Integer lastGeneratorId = testService.getLastId();
        System.out.println(lastGeneratorId);
    }

}
