package minispring.mvc;

import minispring.http.MiniHttpRequest;
import minispring.http.MiniHttpResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class RequestMappingHandlerAdapter implements MiniHandlerAdapter {
    private final List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
    
    public RequestMappingHandlerAdapter() {
        argumentResolvers.add(new RequestParamArgumentResolver());
        returnValueHandlers.add(new JsonReturnValueHandler());
    }
    
    @Override
    public boolean supports(Object handler) {
        return handler instanceof HandlerExecutionChain;
    }
    
    @Override
    public Object handle(MiniHttpRequest request, MiniHttpResponse response, Object handler) throws Exception {
        HandlerExecutionChain chain = (HandlerExecutionChain) handler;
        Method method = chain.getMethod();
        Object controller = chain.getHandler();
        
        Object[] args = resolveArguments(method, request);
        Object returnValue = method.invoke(controller, args);
        
        handleReturnValue(returnValue, response);
        
        return returnValue;
    }
    
    private Object[] resolveArguments(Method method, MiniHttpRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
            if (resolver != null) {
                args[i] = resolver.resolve(parameter, request);
            } else {
                throw new IllegalArgumentException("No resolver for parameter: " + parameter.getName());
            }
        }
        
        return args;
    }
    
    private HandlerMethodArgumentResolver getArgumentResolver(Parameter parameter) {
        for (HandlerMethodArgumentResolver resolver : argumentResolvers) {
            if (resolver.supports(parameter)) {
                return resolver;
            }
        }
        return null;
    }
    
    private void handleReturnValue(Object returnValue, MiniHttpResponse response) {
        for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if (handler.supports(returnValue)) {
                handler.handle(returnValue, response);
                return;
            }
        }
    }
}