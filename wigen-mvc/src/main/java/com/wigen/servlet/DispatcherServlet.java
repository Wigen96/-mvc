package com.wigen.servlet;

import com.wigen.annotations.Controller;
import com.wigen.annotations.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author wwq
 */
public class DispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private Map<String, Object> controllerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1.加载配置
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.初始化相关类
        doScanner(properties.getProperty("scanPackage"));

        // 3.实例化, 加载到ioc容器
        doInstance();

        // 4.初始化HandlerMapping
        initHandlerMapping();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // do dispatch 处理请求
        doDispatch(req, resp);
    }

    /**
     * 加载配置文件, 加到properties
     * @param location
     */
    private void doLoadConfig(String location) {
        System.out.println("location: " + location);
        location = location.replace("classpath:", "");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(location);
        if (in == null) {
            return;
        }
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫包, classNames添加类名
     * @param packageName
     */
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replace(".", "/"));
        File files = new File(url.getFile());
        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 实例化类, 加载到ioc容器
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                // 通过反射实例化类
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    ioc.put(toLowFirstChar(clazz.getName()), clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 首字母转小写
     * @param str
     * @return
     */
    private String toLowFirstChar(String str) {
        if (str == null) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(str);
    }

    /**
     * 初始化handlerMapping handlerMapping(url, method)  controller(url,clazz.instance)
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<? extends Object> clazz = entry.getValue().getClass();

                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                String baseUrl = "";

                // 扫类的requestMapping注解
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = annotation.value();
                }

                // 扫方法的requestMapping注解
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    String url = annotation.value();
                    url = baseUrl.concat(url);
                    handlerMapping.put(url, method);
                    controllerMap.put(url, clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理请求
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        if (handlerMapping.isEmpty()) {
            return;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replace(contextPath, "");

        if (!handlerMapping.containsKey(url)) {
            System.out.println("404 not found!");
        }

        Method method = handlerMapping.get(url);

        // 获取方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        // 获取请求的参数
        Map<String, String[]> paramMap = req.getParameterMap();

        // 参数值保存
        Object[] objs = new Object[parameterTypes.length];

        // 循环参数类型 保存
        for (int i = 0; i < parameterTypes.length; i++) {
            String paramType = parameterTypes[i].getSimpleName();

            if (paramType.equals("HttpServletRequest")) {
                objs[i] = req;
                continue;
            }

            if (paramType.equals("HttpServletResponse")) {
                objs[i] = resp;
                continue;
            }

            if (paramType.equals("String")) {
                for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                    String value = Arrays.toString(entry.getValue());
                    objs[i] = value;
                }
            }
        }

        try {
            method.invoke(controllerMap.get(url), objs);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
