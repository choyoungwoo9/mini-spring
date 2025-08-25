package simple_qualifier_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Simple Qualifier App 테스트 ===");
            
            MiniApplicationContext context = new MiniApplicationContext(AppConfig.class);
            
            // 기본 MessageService 테스트
            MessageService defaultService = context.getBean(MessageService.class);
            System.out.println("Default service: " + defaultService.getMessage());
            
            // Client 테스트 - qualifier가 붙은 MessageService 주입
            Client client = context.getBean(Client.class);
            client.printMessage();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 