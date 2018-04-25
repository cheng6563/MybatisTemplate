package com.mybatistemplate.springdemo;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by leicheng on 2016/7/13.
 */
public interface TestService {

    Country getById(Integer id);

    List<Country> testSelect(Map<String, Object> map,int pageStart,int pageSize);

    Integer getLastId();
}
