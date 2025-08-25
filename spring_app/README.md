# Spring Boot DI 테스트 앱

## 목적
실제 스프링 부트를 사용해서 Primary, Qualifier, 생성자/필드 주입을 테스트합니다.

## 프로젝트 구조
```
spring_app/
├── build.gradle
├── settings.gradle
└── src/main/java/com/example/springapp/
    ├── SpringAppApplication.java (메인 클래스)
    ├── Formatter.java (인터페이스)
    ├── SimpleFormatter.java (@Primary)
    ├── MarkdownFormatter.java (@Qualifier("markdown"))
    ├── Clock.java (서비스)
    ├── MemoService.java (생성자+필드 주입 혼용)
    └── TestRunner.java (테스트 실행)
```

## 실행 방법

### 1. Gradle로 실행
```bash
cd spring_app
./gradlew bootRun
```

### 2. IntelliJ에서 실행
- `SpringAppApplication.java`의 main 메서드 실행
- 또는 Gradle 탭에서 `bootRun` 태스크 실행

## 테스트 시나리오

### 1. Primary 테스트
- `SimpleFormatter`가 `@Primary`로 기본 선택됨
- `Formatter` 타입으로 조회 시 자동 선택

### 2. Qualifier 테스트  
- `MarkdownFormatter`가 `@Qualifier("markdown")`으로 특정 선택
- 명시적으로 특정 구현체 주입

### 3. 주입 방식 혼용 테스트
- `MemoService`에서 생성자 주입과 필드 주입 혼용
- 생성자: `@Qualifier("markdown") Formatter`
- 필드: `@Autowired Clock`

## 예상 결과
```
=== 스프링 부트 DI 테스트 시작 ===

1. Primary Formatter 테스트:
Default formatter: Hello

2. Qualifier Formatter 테스트:
Markdown formatter: **Hello**

3. MemoService 테스트:
Memo: [2024-01-XX...] **Hello World**

=== 테스트 완료 ===
```

## 추가 테스트 아이디어

### 중복 Primary 테스트
```java
@Component @Primary
public class AnotherFormatter implements Formatter { }
// 에러: @Primary가 둘 이상이면 예외 발생
```

### 중복 Qualifier 테스트
```java
@Component @Qualifier("markdown")
public class AnotherMarkdownFormatter implements Formatter { }
// 에러: 같은 qualifier 이름이 둘 이상이면 예외 발생
```

이 앱으로 실제 스프링의 DI 동작을 확인하고, mini-spring 구현 시 참고할 수 있습니다! 