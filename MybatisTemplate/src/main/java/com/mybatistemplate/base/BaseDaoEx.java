package com.mybatistemplate.base;

import com.mybatistemplate.core.TemplateMethod;
import com.mybatistemplate.core.TemplateMethodType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by leicheng on 2016/7/13.
 */
public interface BaseDaoEx<ET,PT> extends BaseDao<ET,PT> {

    @TemplateMethod(TemplateMethodType.InsertBatch)
    @Insert(value = "")
    long insertBatch(@Param("list") List<ET> list);

    @TemplateMethod(TemplateMethodType.UpdateBatch)
    @Update(value = "")
    long updateBatch(@Param("list") List<ET> list);

}
