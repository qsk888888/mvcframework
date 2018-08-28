package com.fw.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by qsk on 2018/8/27.
 */
@Target({ElementType.TYPE, ElementType.METHOD})//声明在类上和方法上使用
@Retention(RetentionPolicy.RUNTIME)
@Documented //表示配置是可见的
public @interface FWRequestMapping {
    //注解写法
    String value() default "";
}
