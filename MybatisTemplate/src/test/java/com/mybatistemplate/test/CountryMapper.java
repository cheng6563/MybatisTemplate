package com.mybatistemplate.test;

import com.mybatistemplate.base.BaseDao;
import com.mybatistemplate.base.BaseDaoEx;

import java.util.List;
import java.util.Map;

/**
 * Created by leicheng on 2016/7/12.
 */
public interface CountryMapper extends BaseDaoEx<Country,Integer> {
    public List<Country> testSelect(Map<String,Object> map);
    int testSelectInt();
}
