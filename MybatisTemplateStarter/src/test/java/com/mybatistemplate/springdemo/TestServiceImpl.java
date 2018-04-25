package com.mybatistemplate.springdemo;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by leicheng on 2016/7/13.
 */
@Repository
public class TestServiceImpl implements TestService {
    @Autowired
    private CountryMapper countryMapper;

    public Country getById(Integer id){
        return countryMapper.getById(id);
    }

    public List<Country> testSelect(Map<String, Object> map,int pageStart,int pageSize){
        PageHelper.startPage(pageStart,pageSize);
        return countryMapper.testSelect(map);
    }

    public Integer getLastId(){
        return countryMapper.getLastGeneratorId();
    }
}
