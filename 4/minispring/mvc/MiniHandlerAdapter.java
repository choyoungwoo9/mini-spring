package minispring.mvc;

import minispring.http.MiniHttpRequest;
import minispring.http.MiniHttpResponse;

public interface MiniHandlerAdapter {
    boolean supports(Object handler);
    Object handle(MiniHttpRequest request, MiniHttpResponse response, Object handler) throws Exception;
}