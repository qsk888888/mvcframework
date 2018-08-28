package com.fw.demo.mvc.action;

import com.fw.demo.service.IDemoSerivce;
import com.fw.mvcframework.annotation.FWAutowired;
import com.fw.mvcframework.annotation.FWController;
import com.fw.mvcframework.annotation.FWRequestMapping;
import com.fw.mvcframework.annotation.FWRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by qsk on 2018/8/26.
 */
@FWController
@FWRequestMapping("/demo")
public class DemoAction {

    @FWAutowired
    private IDemoSerivce iDemoSerivce;

    @FWRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @FWRequestParam("name") String name) {
        String result = iDemoSerivce.get(name);
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FWRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @FWRequestParam("a") Integer a, @FWRequestParam("b") Integer b) {
        try {
            response.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
