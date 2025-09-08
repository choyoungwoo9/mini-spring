package app;

import minispring.annotation.MiniComponent;

// 일단 기본 구조만 만들고, AOP 어노테이션은 나중에 추가
@MiniComponent
public class LogAspect {
    
    // @MiniBefore("execution(* app.memo.*Service.*(..))")
    public void before() {
        System.out.println("[AOP] before");
    }
    
    // @MiniAfter("execution(* app.memo.*Service.*(..))")
    public void after() {
        System.out.println("[AOP] after");
    }
}
