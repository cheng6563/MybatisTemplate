package com.mybatistemplate.springdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by leicheng on 2016/7/13.
 */
@Service
public class TestService {
    @Autowired
    private CountryMapper countryMapper;

    public Country getById(Integer id){
        return countryMapper.getById(id);
    }

    public List<Country> testSelect(Map<String, Object> map){
        return countryMapper.testSelect(map);
    }
}
