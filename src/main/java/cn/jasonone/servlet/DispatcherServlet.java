package cn.jasonone.servlet;

import cn.jasonone.annotation.Controller;
import cn.jasonone.annotation.RequestMapping;
import cn.jasonone.handler.HandlerMapping;
import cn.jasonone.handler.HandlerParameters;
import cn.jasonone.model.ControllerDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class DispatcherServlet extends HttpServlet {
    private ApplicationContext applicationContext;

    private HandlerMapping handlerMapping;

    private HandlerParameters handlerParameters;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 找到所有的处理器
        // 2. 找出处理器中所有的url

        String url = req.getRequestURI().substring(req.getContextPath().length());
        // 从映射关系处理器中获得Controller的定义
        ControllerDefinition controllerDefinition = handlerMapping.getController(url);
        if (controllerDefinition == null) {
            resp.sendError(404,"找不到处理器:"+url);
            return;
        }
        // 获取url所映射的方法
        Method method = controllerDefinition.getMethod(url);
        // 获取url所映射的处理器实例
        Object controller = controllerDefinition.getController();
        try {
            Object[] parameters = handlerParameters.getParameters(method, req, resp);
            method.invoke(controller,parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        applicationContext= WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        //从容器中获得映射关系处理器
        handlerMapping=applicationContext.getBean("handlerMapping",HandlerMapping.class);
        handlerParameters=applicationContext.getBean("handlerParameters",HandlerParameters.class);
    }
}
