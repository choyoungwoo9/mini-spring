package app;

import minispring.annotation.*;

@MiniAspect
@MiniComponent
public class LogAspect {
    
    @MiniBefore("app.MemoService.save")
    public void before() {
        System.out.println("[AOP] before");
    }
    
    @MiniAfter("app.MemoService.save")
    public void after() {
        System.out.println("[AOP] after");
    }
}
