package com.example.springapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class TestRunner implements CommandLineRunner {

    @Autowired
    private MemoService memoService;
    
    @Autowired
    private Formatter defaultFormatter;
    
    @Autowired
    @Qualifier("markdown")
    private Formatter markdownFormatter;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 스프링 부트 DI 테스트 시작 ===");
        
        // 1. Primary 테스트 - 기본 Formatter 사용
        System.out.println("\n1. Primary Formatter 테스트:");
        System.out.println("Default formatter: " + defaultFormatter.format("Hello"));
        
        // 2. Qualifier 테스트 - 특정 Formatter 사용
        System.out.println("\n2. Qualifier Formatter 테스트:");
        System.out.println("Markdown formatter: " + markdownFormatter.format("Hello"));
        
        // 3. MemoService 테스트 - 생성자와 필드 주입 혼용
        System.out.println("\n3. MemoService 테스트:");
        System.out.println("Memo: " + memoService.write("Hello World"));
        
        System.out.println("\n=== 테스트 완료 ===");
        
        // 애플리케이션 종료
        System.exit(0);
    }
} 