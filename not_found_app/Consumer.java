package not_found_app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

@MiniComponent
public class Consumer {
    @MiniAutowired
    private NonExistentService service;  // 구현체가 없음!

    public void doWork() {
        service.doSomething();
    }
}