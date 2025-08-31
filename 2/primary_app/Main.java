package primary_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Primary 어노테이션 테스트 ===");
            
            // PrimaryConfig를 사용하여 컨텍스트 초기화
            MiniApplicationContext context = new MiniApplicationContext(PrimaryConfig.class);
            
            // PrimaryService 타입의 빈 조회 - @Primary가 붙은 빈이 자동 선택됨
            PrimaryService service = context.getBean(PrimaryService.class);
            System.out.println("PrimaryService 실행 결과: " + service.serve());
            
            // PrimaryService의 구체적인 구현체 확인
            System.out.println("실제 구현체: " + service.getClass().getSimpleName());
            
        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
