package cn.jasonone.handler;

import cn.jasonone.annotation.Controller;
import cn.jasonone.annotation.RequestMapping;
import cn.jasonone.model.ControllerDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 映射关系处理器
 */
@Component
@Slf4j
public class HandlerMapping implements ApplicationContextAware {
    private List<ControllerDefinition> controllerDefinitions;
    private ApplicationContext applicationContext;

    @PostConstruct // init-method
    public void init(){
        controllerDefinitions=new ArrayList<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
        Set<Map.Entry<String, Object>> values = controllers.entrySet();
        for (Map.Entry<String, Object> entry : values) {
            Object controller=entry.getValue();
            Class<?> controllerType=controller.getClass();
            String[] urlParents = getRequestMapping(controllerType);
            Method[] methods = controllerType.getMethods();
            if (methods != null) {
                // 创建Controller的定义
                ControllerDefinition cd=new ControllerDefinition();
                cd.setController(controller);
                log.info("注册Controller: {}",entry.getKey());

                for (Method method : methods) {
                    //获得方法上RequestMapping注解的URL数组
                    String[] requestMappings = getRequestMapping(method);
                    if (requestMappings != null && requestMappings.length>0) {
                        for (String requestMapping : requestMappings) {
                            if (urlParents.length>0){
                                for (String urlParent : urlParents) {
                                    String url=urlParent+requestMapping;
                                    log.trace("注册URL映射: {}",url);
                                    //注册映射关系到Controller定义上
                                    cd.register(url,method);
                                }
                            }

                        }
                    }
                }
                controllerDefinitions.add(cd);
            }

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    /**
     * 根据URL获得Controller的定义信息
     * @param url
     * @return
     */
    public ControllerDefinition getController(String url){
        for (ControllerDefinition controllerDefinition : controllerDefinitions) {
            if(controllerDefinition.hasRequestMapping(url)){
                return controllerDefinition;
            }
        }
        return null;
    }

    private String[] getRequestMapping(AnnotatedElement annotatedElement) {
        String[] urlParents = {};
        RequestMapping requestMapping = AnnotationUtils.getAnnotation(annotatedElement, RequestMapping.class);
        if (requestMapping != null) {
            String[] value = (String[]) AnnotationUtils.getValue(requestMapping, "value");
            if(value != null && value.length>0){
                urlParents=value;
            }
        }
        return urlParents;
    }
}
