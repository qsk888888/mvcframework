package com.fw.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by qsk on 2018/8/27.
 */
@Target({ElementType.TYPE})//说明注解在哪里可以使用 在类上使用
@Retention(RetentionPolicy.RUNTIME) //注解的生命周期
@Documented //表示注解是可见的
public @interface FWController {
    //注解写法
    String value() default "";
}
