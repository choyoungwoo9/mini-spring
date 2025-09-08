package minispring.aop;

import java.lang.reflect.Method;

public class PointcutMatcher {

    public static boolean matches(String pointcut, Method method, Class<?> targetClass) {
        if (pointcut == null)
            return false;
        String fullMethodName = targetClass.getName() + "." + method.getName();
        return pointcut.equals(fullMethodName);
    }
}
