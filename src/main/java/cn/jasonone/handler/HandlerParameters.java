package cn.jasonone.handler;

import cn.hutool.core.convert.Convert;
import cn.jasonone.annotation.RequestParam;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class HandlerParameters {

    public Object[] getParameters(Method method, HttpServletRequest request, HttpServletResponse response){
        Map<String,String[]> requestParameters=new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()){
            String name= parameterNames.nextElement();
            requestParameters.put(name,request.getParameterValues(name));
        }
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length>0) {
            Object[] args=new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> type = parameter.getType();
                RequestParam requestParam = AnnotationUtils.getAnnotation(parameter, RequestParam.class);
                if (requestParam != null) {
                    String name = (String) AnnotationUtils.getValue(requestParam, "name");
                    if (name != null && !name.isEmpty()) {
                        if(requestParameters.containsKey(name)){
                            String[] values = requestParameters.get(name);
                            if(type.isArray()){
                                args[i]= Convert.convert(type,values);
                            }else{
                                if (values != null && values.length>0) {
                                    args[i]=Convert.convert(type,values[0]);
                                }
                            }
                        }
                    }
                }
            }
            return args;
        }
        return new Object[0];
    }
}
