package cn.jasonone.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
@Data
public class ControllerDefinition {
    @Setter(AccessLevel.NONE)
    private Map<String, Method> mappings=new HashMap<>();

    private Object controller;

    public boolean hasRequestMapping(String url){
        return mappings.containsKey(url);
    }

    public Method getMethod(String url){
        return mappings.get(url);
    }

    public void register(String url,Method method){
        mappings.put(url,method);
    }


}
