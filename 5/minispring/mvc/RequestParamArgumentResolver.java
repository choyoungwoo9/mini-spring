package minispring.mvc;

import minispring.annotation.MiniRequestParam;
import minispring.http.MiniHttpRequest;

import java.lang.reflect.Parameter;

public class RequestParamArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supports(Parameter parameter) {
        return parameter.isAnnotationPresent(MiniRequestParam.class);
    }
    
    @Override
    public Object resolve(Parameter parameter, MiniHttpRequest request) throws Exception {
        MiniRequestParam annotation = parameter.getAnnotation(MiniRequestParam.class);
        String paramName = annotation.value().isEmpty() ? parameter.getName() : annotation.value();
        
        String value = request.getQueryParam(paramName);
        if (value == null) {
            throw new IllegalArgumentException("Required parameter '" + paramName + "' is not present");
        }
        
        Class<?> paramType = parameter.getType();
        if (paramType == String.class) {
            return value;
        } else if (paramType == int.class || paramType == Integer.class) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to int");
            }
        }
        
        throw new IllegalArgumentException("Unsupported parameter type: " + paramType.getName());
    }
}