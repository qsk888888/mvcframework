package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2018/8/27.
 */
@Target({ElementType.TYPE, ElementType.METHOD})//声明在类上和方法上使用
@Retention(RetentionPolicy.RUNTIME)
@Documented //表示配置是可见的
public @interface GPRequestMapping {
    //注解写法
    String value() default "";
}
