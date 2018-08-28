package com.fw.demo.service.impl;

import com.fw.demo.service.IDemoSerivce;
import com.fw.mvcframework.annotation.FWService;

/**
 * Created by qsk on 2018/8/27.
 */
@FWService
public class IDemoSerivceImpl implements IDemoSerivce {
    @Override
    public String get(String name) {
        return "my name is  " + name;
    }
}
