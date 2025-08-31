package simple_qualifier_app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Simple Qualifier App 테스트 ===");
            
            MiniApplicationContext context = new MiniApplicationContext(AppConfig.class);

            MessageService defaultService = context.getBean(MessageService.class, "welcome");
//            MessageService defaultService = context.getBean(DefaultMessageService.class);
//            MessageService defaultService = context.getBean(MessageService.class);
            System.out.println("Default service: " + defaultService.getMessage());

            //웰컴 클라이언트(by qualifier) 주입 확인
            Client client = context.getBean(Client.class);
            client.printMessage();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 