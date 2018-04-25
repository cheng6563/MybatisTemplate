package com.mybatistemplate.springdemo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;

/**
 * Created by leicheng on 2016/7/13.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Test1OnSpringBoot {


    @Autowired
    private TestService testService;

    @Before
    public void init(){
    }

    @Test
    public void test1(){
        Country rs = testService.getById(1);
        System.out.println(rs);
    }

    @Test
    public void test2(){
        List<Country> rs = testService.testSelect(new HashMap<String, Object>(),1,3);
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
