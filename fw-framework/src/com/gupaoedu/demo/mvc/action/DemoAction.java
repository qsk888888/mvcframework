package com.gupaoedu.demo.mvc.action;

import com.gupaoedu.demo.service.IDemoSerivce;
import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2018/8/26.
 */
@GPController
@GPRequestMapping("/demo")
public class DemoAction {

    @GPAutowired
    private IDemoSerivce iDemoSerivce;

    @GPRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @GPRequestParam("name") String name) {
        String result = iDemoSerivce.get(name);
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @GPRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b) {
        try {
            response.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
