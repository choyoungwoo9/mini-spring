package minispring.aop;

import minispring.container.MiniApplicationContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class AopProxy implements InvocationHandler {
    private final Object target;
    private final List<MiniApplicationContext.AspectInfo> aspects;
    
    public AopProxy(Object target, List<MiniApplicationContext.AspectInfo> aspects) {
        this.target = target;
        this.aspects = aspects;
    }
    
    public static Object createProxy(Object target, List<MiniApplicationContext.AspectInfo> aspects) {
        return Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            new AopProxy(target, aspects)
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Before 어드바이스 실행
        for (MiniApplicationContext.AspectInfo aspect : aspects) {
            for (MiniApplicationContext.AdviceInfo advice : aspect.getBeforeAdvices()) {
                if (advice.matches(method, target.getClass())) {
                    advice.getMethod().invoke(aspect.getAspectInstance());
                }
            }
        }
        
        // 원본 메서드 실행
        Object result = method.invoke(target, args);
        
        // After 어드바이스 실행
        for (MiniApplicationContext.AspectInfo aspect : aspects) {
            for (MiniApplicationContext.AdviceInfo advice : aspect.getAfterAdvices()) {
                if (advice.matches(method, target.getClass())) {
                    advice.getMethod().invoke(aspect.getAspectInstance());
                }
            }
        }
        
        return result;
    }
}
