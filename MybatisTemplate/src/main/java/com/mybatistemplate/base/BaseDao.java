package com.mybatistemplate.base;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.SqlSession;

public interface BaseDao<ET,PT> {
    @TemplateMethod
    @Delete(value = "")
    boolean deleteById(PT id);

    @TemplateMethod
    @Insert(value = "")
    boolean insert(ET entity);

    @TemplateMethod
    @Select(value = "")
    ET getById(PT id);

    @TemplateMethod
    @Update(value = "")
    boolean update(ET entity);


    @TemplateMethod
    @Select(value = "")
    List<ET> findByExample(ET example);

    @TemplateMethod
    @Select(value = "")
    List<ET> findByMap(Map<String, ?> paramMap);
}
