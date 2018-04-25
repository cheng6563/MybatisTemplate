package com.mybatistemplate.starter;

import com.github.pagehelper.autoconfigure.PageHelperProperties;
import com.mybatistemplate.core.IdGeneratorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MapperHelper.PAGEHELPER_PREFIX)
public class MapperHelper extends com.mybatistemplate.core.MapperHelper {
    public static final String PAGEHELPER_PREFIX = "mybatistemplate";
}
