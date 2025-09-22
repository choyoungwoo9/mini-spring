package minispring.mvc;

import minispring.http.MiniHttpRequest;

import java.lang.reflect.Parameter;

public interface HandlerMethodArgumentResolver {
    boolean supports(Parameter parameter);
    Object resolve(Parameter parameter, MiniHttpRequest request) throws Exception;
}