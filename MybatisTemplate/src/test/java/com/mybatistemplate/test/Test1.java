package com.mybatistemplate.test;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by leicheng on 2016/7/12.
 */
public class Test1 {
    private SqlSession sqlSession;
    private CountryMapper mapper;

    @Before
    public void init() {
        sqlSession = MybatisHelper.getSqlSession();
        mapper = sqlSession.getMapper(CountryMapper.class);
    }

    @After
    public void clean(){
        sqlSession.rollback();
    }


    @Test
    public void testInsert() {
        boolean insert = mapper.insert(new Country() {{
            setCountryname("AAA");
            setCountrycode("aaa");
        }});
        assert insert;
    }


    @Test
    public void testGetLastGeneratorId(){
        testInsert();
        Integer lastGeneratorId = mapper.getLastGeneratorId();
        assert lastGeneratorId!=null;
    }

    @Test
    public void testGetById() {
        testInsert();
        Country rs = mapper.getById(mapper.getLastGeneratorId());
        assert (rs!=null);
    }


    @Test
    public void testUpdate() {
        testInsert();
        Country country = mapper.getById(mapper.getLastGeneratorId());
        country.setCountryname("BBB");
        country.setCountrycode("bbb");
        boolean update = mapper.update(country);
        assert update;
        country = mapper.getById(mapper.getLastGeneratorId());
        assert Objects.equals(country.getCountryname(),"BBB")&&Objects.equals(country.getCountrycode(),"bbb");
    }

    @Test
    public void testDelete() {
        testInsert();
        Integer id = mapper.getLastGeneratorId();
        boolean rs = mapper.deleteById(mapper.getLastGeneratorId());
        Country country = mapper.getById(id);
        assert rs && country == null;
    }

    @Test
    public void testFindByMap() {
        testInsert();
        testInsert();
        List<Country> countrys = mapper.findByMap(new HashMap<String, Object>() {{
            put("countryname", "AAA");
        }});
        assert countrys.size() == 2;
    }

    @Test
    public void testFindByExample() {
        testInsert();
        testInsert();
        List<Country> countrys = mapper.findByExample(new Country(){{
            setCountrycode("aaa");
        }});
        assert countrys.size() == 2;
    }

}
