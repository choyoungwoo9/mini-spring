package minispring.mvc;

import minispring.http.MiniHttpResponse;

public interface HandlerMethodReturnValueHandler {
    boolean supports(Object returnValue);
    void handle(Object returnValue, MiniHttpResponse response);
}