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

    @Before
    public void init() {
        sqlSession = MybatisHelper.getSqlSession();
    }

    @Test
    public void testNative() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List rs = mapper.testSelect(new HashMap<String, Object>(){{
            put("start","0");
            put("end","998");
        }});
        System.out.println(rs);
    }

    @Test
    public void testGetById() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country rs = mapper.getById(1);
        System.out.println(rs);
    }

    @Test
    public void testInsert() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        boolean insert = mapper.insert(new Country() {{
            setCountryname("FFFFF");
            setCountrycode("FFFFF");
        }});
        System.out.println(insert);
        sqlSession.commit();

    }

    @Test
    public void testUpdate() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = mapper.getById(1);
        country.setCountryname("BBBBB");
        country.setCountrycode("BBBB");
        boolean update = mapper.update(country);
        sqlSession.commit();
        System.out.println(update);
    }

    @Test
    public void testDelete() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        mapper.deleteById(193);
        sqlSession.commit();
    }

    @Test
    public void testFindByMap() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List<Country> id = mapper.findByMap(new HashMap<String, Object>() {{
            //put("id", 200);
            put("countryname", "FFFFF");
        }});
        System.out.println(id);
    }

    @Test
    public void testFindByExample() {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List<Country> id = mapper.findByExample(new Country(){{
            //setId(200);
            setCountrycode("FFFFF");
        }});
        System.out.println(id);
    }
}
