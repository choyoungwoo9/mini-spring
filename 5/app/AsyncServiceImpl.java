package app;

import minispring.annotation.MiniAsync;
import minispring.annotation.MiniComponent;

@MiniComponent
public class AsyncServiceImpl implements AsyncService {

    @Override
    @MiniAsync
    public void slowTask() {
        System.out.println("slowTask start thread=" + Thread.currentThread().getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        System.out.println("slowTask end");
    }
}
