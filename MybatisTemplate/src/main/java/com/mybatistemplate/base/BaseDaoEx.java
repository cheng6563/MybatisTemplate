package com.mybatistemplate.base;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by leicheng on 2016/7/13.
 */
public interface BaseDaoEx<ET,PT> extends BaseDao<ET,PT> {

    @TemplateMethod
    @Insert(value = "")
    long insertBatch(List<ET> list);

    @TemplateMethod
    @Update(value = "")
    long updateBatch(List<ET> list);

    @TemplateMethod
    @Select(value = "")
    PT getLastGeneratorId();
}
