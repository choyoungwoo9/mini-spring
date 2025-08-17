package 모호_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 모호성 테스트 ===");
        try {
            var ctx = new MiniApplicationContext(AppConfig.class);
            var consumer = ctx.getBean(Consumer.class);
            consumer.doWork();
        } catch (Exception e) {
            System.err.println("예상된 모호성 예외: " + e.getMessage());
        }
    }
}