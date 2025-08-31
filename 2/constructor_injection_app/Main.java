package constructor_injection_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 생성자 주입 예제 실행 ===");
        
        // 애플리케이션 컨텍스트 초기화
        MiniApplicationContext context = new MiniApplicationContext(AppConfig.class);
        
        // UserService 빈 가져오기
        UserService userService = context.getBean(UserService.class);
        
        // UserService 메서드 호출
        String userInfo = userService.getUserInfo("user123");
        
        System.out.println("실행 결과: " + userInfo);
    }
}
