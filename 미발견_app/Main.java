package 미발견_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 미발견 테스트 ===");
        try {
            var ctx = new MiniApplicationContext(AppConfig.class);
            var consumer = ctx.getBean(Consumer.class);
            consumer.doWork();
        } catch (Exception e) {
            System.err.println("예상된 미발견 예외: " + e.getMessage());
        }
    }
}