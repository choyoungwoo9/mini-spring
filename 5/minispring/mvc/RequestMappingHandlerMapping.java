package minispring.mvc;

import minispring.annotation.MiniController;
import minispring.annotation.MiniGetMapping;
import minispring.annotation.MiniRequestMapping;
import minispring.container.MiniApplicationContext;
import minispring.http.MiniHttpRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping implements MiniHandlerMapping {
    private final Map<String, HandlerExecutionChain> handlerMap = new HashMap<>();
    
    public RequestMappingHandlerMapping(MiniApplicationContext context) {
        initHandlerMethods(context);
    }
    
    private void initHandlerMethods(MiniApplicationContext context) {
        for (Object bean : context.getAllBeans()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(MiniController.class)) {
                String basePath = "";
                if (clazz.isAnnotationPresent(MiniRequestMapping.class)) {
                    basePath = clazz.getAnnotation(MiniRequestMapping.class).value();
                }
                
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(MiniGetMapping.class)) {
                        String methodPath = method.getAnnotation(MiniGetMapping.class).value();
                        String fullPath = basePath + methodPath;
                        String key = "GET:" + fullPath;
                        handlerMap.put(key, new HandlerExecutionChain(bean, method));
                    }
                }
            }
        }
    }
    
    @Override
    public HandlerExecutionChain getHandler(MiniHttpRequest request) {
        String key = request.getMethod() + ":" + request.getPath();
        return handlerMap.get(key);
    }
}