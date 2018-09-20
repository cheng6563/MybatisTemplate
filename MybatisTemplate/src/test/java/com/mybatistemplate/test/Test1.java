package com.mybatistemplate.test;

import com.mybatistemplate.core.ConditionSymbol;
import com.mybatistemplate.core.FindWrapper;
import com.mybatistemplate.util.Getter;
import com.mybatistemplate.util.Pair;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.SqlSession;
import org.junit.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by leicheng on 2016/7/12.
 */
public class Test1 {
    private static SqlSession sqlSession;
    private static CountryMapper mapper;

    @BeforeClass
    public static void init() throws SQLException {
        sqlSession = MybatisHelper.getSqlSession();
        Connection connection = sqlSession.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("create table country(" +
                "id int primary key auto_increment," +
                "country_name varchar(50)," +
                "country_code varchar(50)," +
                "ver int" +
                ")");
        mapper = sqlSession.getMapper(CountryMapper.class);
    }

    @After
    public void clean() {
        sqlSession.rollback();
        //sqlSession.commit();
    }

    @Test
    public void testSql() {
        XPathParser xPathParser = new XPathParser("<select>select * from country <where><if test=\"start != null and end != null\">id between #{start} and #{end}</if></where></select>");
        XNode xNode = xPathParser.evalNode("select");
        SqlSource sqlSource = new XMLScriptBuilder(sqlSession.getConfiguration(), xNode).parseScriptNode();
        TextSqlNode textSqlNode = new TextSqlNode("select * from country <where><if test=\"start != null and end != null\">id between #{start} and #{end}</if>");
        DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(sqlSession.getConfiguration(), textSqlNode);
        System.out.println(textSqlNode);
    }

    @Test
    public void testWrapper() {
        mapper.insert(new Country() {{
            setCountryname("AAA");
            setCountrycode("aaa");
            setVer(0);
        }});
        mapper.insert(new Country() {{
            setCountryname("BBB");
            setCountrycode("bbb");
            setVer(0);
        }});
        mapper.insert(new Country() {{
            setCountryname("CCC");
            setCountrycode("ccc");
            setVer(0);
        }});
        List<Country> countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.EQ, "BBB"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 1);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");

        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.GT, "AAA"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 2);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");

        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.GT_EQ, "AAA"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 3);
        Assert.assertEquals(countries.get(0).getCountryname(), "AAA");

        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.NOT_EQ, "AAA"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 2);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");


        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.IS_NOT_NULL, "AAA"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 3);
        Assert.assertEquals(countries.get(0).getCountryname(), "AAA");


        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.BETWEEN, new Pair<>("BBB", "CCC")));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 2);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");


        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.IN, Arrays.asList("CCC", "BBB")));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 2);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");


        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class).addCondition("countryname", ConditionSymbol.IN, null));
        Assert.assertTrue(countries.isEmpty());


        countries = mapper.testWrapper(new FindWrapper<Country>(Country.class)
                .addCondition("countryname", ConditionSymbol.LIKE, "%")
                .setOrderProp("countryname", false));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 3);
        Assert.assertEquals(countries.get(0).getCountryname(), "CCC");
    }

    @Test
    public void testWrapper2() {
        mapper.insert(new Country() {{
            setCountryname("AAA");
            setCountrycode("aaa");
            setVer(0);
        }});
        mapper.insert(new Country() {{
            setCountryname("BBB");
            setCountrycode("bbb");
            setVer(0);
        }});
        mapper.insert(new Country() {{
            setCountryname("CCC");
            setCountrycode("ccc");
            setVer(0);
        }});
        List<Country> countries = mapper.findByFindWrapper(new FindWrapper<Country>().addCondition(new Getter<Country>() {
            @Override
            public Object get(Country entity) {
                return entity.getCountryname();
            }
        }, ConditionSymbol.EQ, "BBB"));
        Assert.assertFalse(countries.isEmpty());
        Assert.assertEquals(countries.size(), 1);
        Assert.assertEquals(countries.get(0).getCountryname(), "BBB");

    }


    @Test
    public void testInsert() {
        boolean insert = mapper.insert(new Country() {{
            setCountryname("AAA");
            setCountrycode("aaa");
            setVer(0);
        }});
        assert insert;
    }


    @Test
    public void testGetLastGeneratorId() {
        testInsert();
        Integer lastGeneratorId = mapper.getLastGeneratorId();
        assert lastGeneratorId != null;
    }

    @Test
    public void testGetById() {
        testInsert();
        Country rs = mapper.getById(mapper.getLastGeneratorId());
        assert (rs != null);
    }


    @Test
    public void testUpdate() {
        testInsert();
        Country country = mapper.getById(mapper.getLastGeneratorId());
        country.setCountryname("BBB");
        country.setCountrycode("bbb");
        boolean update = mapper.update(country);
        assert update;
        boolean update2 = mapper.update(country);
        assert !update2;
        country = mapper.getById(mapper.getLastGeneratorId());
        assert Objects.equals(country.getCountryname(), "BBB");
        assert Objects.equals(country.getCountrycode(), "bbb");
        assert country.getVer() == 1;
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
        assert countrys.size() >= 2;
    }

    @Test
    public void testFindByExample() {
        testInsert();
        testInsert();
        List<Country> countrys = mapper.findByExample(new Country() {{
            setCountrycode("aaa");
        }});
        assert countrys.size() >= 2;
    }


}
