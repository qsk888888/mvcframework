package com.fw.mvcframework.servlet;

import com.fw.mvcframework.annotation.FWAutowired;
import com.fw.mvcframework.annotation.FWController;
import com.fw.mvcframework.annotation.FWRequestMapping;
import com.fw.mvcframework.annotation.FWService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/8/26.
 */
public class FWDispatchServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<>();
    //线程安全的问题
    private Map<String, Object> ioc = new HashMap<String, Object>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private Map<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //任务调度和派遣
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!!!!!!");
        }
        if ("/favicon.ico".equals(url)) { //还不知怎么处理
            return;
        }

        Method method = this.handlerMapping.get(url);

        Map<String, String[]> params = req.getParameterMap();

        System.out.print(method + "\n");
        //用反射调用这个方法
        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());

        method.invoke(ioc.get(beanName), req, resp, params.get("name")[0]);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.扫描所有相关的类
        doScanner(contextConfig.getProperty("scanpackage"));
        //3.实例化所有的相关的class,并且放入IOC容器中（Map）
        doInstance();
        //4.依赖注入，把需要需要赋值的字段自动赋值
        doAutowired();
        //=================spring完成=================

        //=================springMVC==================
        //5.初始化HandlerMapping(把Controller中的url和Method进行一对一的映射)
        initHandlerMapping();

        System.out.print("哈哈哈，真开心，FW Spring MVC init成功！！！\n");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(FWController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(FWRequestMapping.class)) {
                FWRequestMapping requestMapping = clazz.getAnnotation(FWRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(FWRequestMapping.class)) {
                    continue;
                }

                FWRequestMapping requestMapping = method.getAnnotation(FWRequestMapping.class);
                String url = requestMapping.value();
                url = (baseUrl + "/" + url).replaceAll("/+", "/");

                handlerMapping.put(url, method);
            }

        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(FWAutowired.class)) {
                    continue;
                }

                FWAutowired autowired = field.getAnnotation(FWAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                //不管是不是private,protected,default,只要加了注解都会自动赋值，有点像强吻
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //只有加了注解的类才会实例化
                if (clazz.isAnnotationPresent(FWController.class)) {
                    Object instance = clazz.newInstance();
                    //spring 中的key有三种形式
                    //1.ioc容器中的每一个Bean都有一个唯一的Id,beanName  beanName默认是采用类名首字母小写
                    String beanName = lowerFirstCase(clazz.getSimpleName());

                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(FWService.class)) {
                    //2.如果采用自定义beanName,优先采用自定义的beanName
                    FWService service = clazz.getAnnotation(FWService.class);
                    String beanName = service.value();
                    if ("".equals(beanName.trim())) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();

                    ioc.put(beanName, instance);

                    //3.用接口的全名称作为key,用实现类的实例作为值
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 类名首字母小写 骚操作
     *
     * @param simpleName
     * @return
     */
    private String lowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanpackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanpackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            //如果是文件夹
            if (file.isDirectory()) {
                //继续递归扫描子包
                doScanner(scanpackage + "." + file.getName());
            } else {
                String className = (scanpackage + "." + file.getName().replace(".class", ""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        //把配置文件读取进来，把key保留下来
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Handler {

        protected Object controller;//保存方法对应的实例
        protected Method method;   //保存映射的方法
        protected Pattern pattern; //
        protected Map<String, Integer> paramIndexMapping; //参数顺序

        /**
         * 构造一个Handler基本参数
         *
         * @param controller
         * @param method
         * @param pattern
         */
        public Handler(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            paramIndexMapping = new HashMap<String, Integer>();

        }

    }
}
