package com.wangyaochong.mvc;

import com.wangyaochong.anno.WController;
import com.wangyaochong.anno.WRequestMapping;
import com.wangyaochong.ioc.WApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangyaochong
 * @date 2020/3/25 21:18
 */
//@WebServlet("/")
@Slf4j
public class WDispatcherServlet extends HttpServlet {
    final String LOCATION = "contextConfigLocation";
    List<WHandlerMapping> handlerMappings = new ArrayList<>();
    Map<WHandlerMapping, WHandlerAdapter> handlerAdapterMap = new HashMap<>();
    List<WViewResolver> viewResolverList = new ArrayList<>();
    WApplicationContext context;


    @Override
    public void init(ServletConfig config) throws ServletException {
        context = new WApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }

    void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WHandlerMapping handlerMapping = getHandlerMapping(request);
        WHandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);
        WModelAndView modelAndView = handlerAdapter.handle(request, response, handlerMapping);
        processDispatchResult(request, response, modelAndView);
    }


    WHandlerMapping getHandlerMapping(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        uri = uri.replaceFirst(contextPath, "").replaceAll("/+", "/");
        for (WHandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(uri);
            if (matcher.matches()) {
                return handlerMapping;
            }
        }
        throw new RuntimeException("404 not found");
    }

    WHandlerAdapter getHandlerAdapter(WHandlerMapping handlerMapping) {
        WHandlerAdapter wHandlerAdapter = handlerAdapterMap.get(handlerMapping);
        if (wHandlerAdapter.supports(handlerMapping)) {
            return wHandlerAdapter;
        }
        throw new RuntimeException("没有处理器");
    }


    void processDispatchResult(HttpServletRequest request, HttpServletResponse response, WModelAndView mv) throws Exception {
        if (null == mv) {
            return;
        }
        for (WViewResolver viewResolver : viewResolverList) {
            WView view = viewResolver.resolveViewName(mv.getViewName(), null);
            if (view != null) {
                view.render(mv.getModel(), request, response);
                return;
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


    void initStrategies(WApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);
    }

    void initHandlerMappings(WApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(WController.class)) {
                    continue;
                }
                String baseUrl = "";
                if (clazz.isAnnotationPresent(WRequestMapping.class)) {
                    WRequestMapping requestMapping = clazz.getAnnotation(WRequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    method.setAccessible(true);
                    if (!method.isAnnotationPresent(WRequestMapping.class)) {
                        continue;
                    }
                    WRequestMapping requestMapping = method.getAnnotation(WRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new WHandlerMapping(controller, method, pattern));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initHandlerAdapters(WApplicationContext context) {
        for (WHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapterMap.put(handlerMapping, new WHandlerAdapter());
        }
    }


    private void listFile(File classPath) {
        for (File file : Objects.requireNonNull(classPath.listFiles())) {
            if (file.isDirectory()) {
                listFile(file);
            } else {
                System.out.println(file.getName());
            }
        }
    }

    void initViewResolvers(WApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        URL resource = this.getClass().getResource("/");

        listFile(new File(resource.getFile()));

        log.info(resource.toString());
        log.info("resource.getFile=" + resource.getFile());
        String templateRootPath = this.getClass().getResource("/" + templateRoot).getFile();
        log.info("templateRootPath=" + templateRootPath);
        File file = new File(templateRootPath);
        for (File listFile : file.listFiles()) {
            WViewResolver e = new WViewResolver();
//            e.setWebAppRoot(listFile);
            this.viewResolverList.add(e);
        }

    }

    void initMultipartResolver(WApplicationContext context) {
    }

    void initLocaleResolver(WApplicationContext context) {
    }

    void initThemeResolver(WApplicationContext context) {
    }


    void initHandlerExceptionResolvers(WApplicationContext context) {
    }

    void initRequestToViewNameTranslator(WApplicationContext context) {
    }


    void initFlashMapManager(WApplicationContext context) {
    }
}
