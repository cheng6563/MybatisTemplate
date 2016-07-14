package com.mybatistemplate.test;

import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

/**
 * Created by leicheng on 2016/7/12.
 */
public class Test1 {
    SqlSession sqlSession;
    private CountryMapper mapper;

    @Before
    public void init() {
        sqlSession = MybatisHelper.getSqlSession();
        mapper = sqlSession.getMapper(CountryMapper.class);
    }

    @Test
    public void testNative() {
        List rs = mapper.testSelect(new HashMap<String, Object>(){{
            put("start","0");
            put("end","998");
        }});
        System.out.println(rs);
    }

    @Test
    public void testGetById() {
        Country rs = mapper.getById(1);
        System.out.println(rs);
    }

    @Test
    public void testInsert() {
        boolean insert = mapper.insert(new Country() {{
            setCountryname("FFFFF");
            setCountrycode("FFFFF");
            setId(501);
        }});
        System.out.println(insert);
        sqlSession.commit();

    }

    @Test
    public void testUpdate() {
        Country country = mapper.getById(1);
        country.setCountryname("BBBBB");
        country.setCountrycode("BBBB");
        boolean update = mapper.update(country);
        sqlSession.commit();
        System.out.println(update);
    }

    @Test
    public void testDelete() {
        mapper.deleteById(193);
        sqlSession.commit();
    }

    @Test
    public void testFindByMap() {
        /*List<Country> id = mapper.findByMap(new HashMap<String, Object>() {{
            //put("id", 200);
            put("countryname", "FFFFF");
        }});*/
        List<Country> id = mapper.findByMap(null);
        System.out.println(id);
    }

    @Test
    public void testFindByExample() {
        /*List<Country> id = mapper.findByExample(new Country(){{
            //setId(200);
            setCountrycode("FFFFF");
        }});*/
        List<Country> id = mapper.findByExample(null);
        System.out.println(id);
    }

    @Test
    public void testGetLastGeneratorId(){
        testInsert();
        Integer lastGeneratorId = mapper.getLastGeneratorId();
        System.out.println(lastGeneratorId);
    }

    @Test
    public void testSelectInt(){
        int i = mapper.testSelectInt();
        System.out.println(i);
    }
}
