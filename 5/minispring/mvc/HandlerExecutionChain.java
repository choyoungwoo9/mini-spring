package minispring.mvc;

import java.lang.reflect.Method;

public class HandlerExecutionChain {
    private final Object handler;
    private final Method method;

    public HandlerExecutionChain(Object handler, Method method) {
        this.handler = handler;
        this.method = method;
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }
}