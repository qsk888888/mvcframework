package com.gupaoedu.demo.service.impl;

import com.gupaoedu.demo.service.IDemoSerivce;
import com.gupaoedu.mvcframework.annotation.GPService;

/**
 * Created by Administrator on 2018/8/27.
 */
@GPService
public class IDemoSerivceImpl implements IDemoSerivce {
    @Override
    public String get(String name) {
        return "my name is  " + name;
    }
}
