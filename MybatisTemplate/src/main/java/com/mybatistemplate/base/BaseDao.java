package com.mybatistemplate.base;

import java.util.List;
import java.util.Map;

import com.mybatistemplate.core.FindWrapper;
import com.mybatistemplate.core.TemplateMethod;
import com.mybatistemplate.core.TemplateMethodType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface BaseDao<ET, PT> {
    @TemplateMethod(TemplateMethodType.DeleteById)
    @Delete(value = "")
    boolean deleteById(PT id);

    @TemplateMethod(TemplateMethodType.Insert)
    @Insert(value = "")
    boolean insert(ET entity);

    @TemplateMethod(TemplateMethodType.GetById)
    @Select(value = "")
    ET getById(PT id);

    @TemplateMethod(TemplateMethodType.Update)
    @Update(value = "")
    boolean update(ET entity);

    @TemplateMethod(TemplateMethodType.FindByExample)
    @Select(value = "")
    List<ET> findByExample(ET example);

    @TemplateMethod(TemplateMethodType.FindByMap)
    @Select(value = "")
    List<ET> findByMap(Map<String, ?> paramMap);

    @TemplateMethod(TemplateMethodType.GetLastGeneratorId)
    @Select(value = "")
    PT getLastGeneratorId();

    @TemplateMethod(TemplateMethodType.FindByFindWrapper)
    @Select(value = "")
    List<ET> findByFindWrapper(FindWrapper<ET> findWrapper);
}
