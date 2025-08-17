package 모호_app;

import minispring.container.MiniApplicationContext;
import minispring.exception.NoUniqueBeanException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 모호성 테스트 ===");
        try {
            var ctx = new MiniApplicationContext(AppConfig.class);
            var consumer = ctx.getBean(Consumer.class);
            consumer.doWork();
        } catch (Exception e) {
            System.err.println("발생한 예외: " + e.getClass().getSimpleName());
            System.err.println("예외 메시지: " + e.getMessage());
            
            // NoUniqueBeanException과 같은지 확인
            if (e instanceof NoUniqueBeanException) {
                System.out.println("✅ 올바른 NoUniqueBeanException 발생!");
            } else {
                System.out.println("❌ 다른 예외 발생: " + e.getClass().getName());
            }
        }
    }
}