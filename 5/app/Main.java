package app;

import app.AsyncService;
import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new MiniApplicationContext(AppConfig.class);
        var svc = ctx.getBean(AsyncService.class);

        svc.slowTask();
        System.out.println("main thread=" + Thread.currentThread().getName());
    }
}
