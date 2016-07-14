package com.mybatistemplate.core;

import java.lang.annotation.*;

/**
 * Created by leicheng on 2016/7/12.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TemplateMethod {
    TemplateMethodType value();
}
