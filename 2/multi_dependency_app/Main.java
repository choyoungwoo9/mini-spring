package multi_dependency_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 다중 의존성 테스트 시작 ===");
        
        try {
            // 애플리케이션 컨텍스트 초기화
            MiniApplicationContext context = new MiniApplicationContext(AppConfig.class);
            
            // BService 빈 가져오기 (A, C에 의존)
            BService bService = context.getBean(BService.class);
            System.out.println("\nBService 결과:");
            System.out.println(bService.getMessage());
            
            // AService와 CService도 직접 가져와서 출력
            AService aService = context.getBean(AService.class);
            CService cService = context.getBean(CService.class);
            
            System.out.println("\nAService 직접 호출: " + aService.getMessage());
            System.out.println("CService 직접 호출: " + cService.getMessage());
            
        } catch (Exception e) {
            System.err.println("\n=== 에러 발생 ===");
            e.printStackTrace();
        }
    }
}
