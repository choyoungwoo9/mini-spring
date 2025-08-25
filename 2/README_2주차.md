# 2주차 — DI 고도화: 생성자/필드/세터 + Primary/Qualifier

## 목표
여러 후보 중 선택 주입이 가능하고, 주입 방식(생성자/필드/세터)을 혼용해도 동작한다.

## 테스트 케이스

### 1. primary_qualifier_app
**목적**: Primary와 Qualifier 기본 동작 테스트
- `@MiniPrimary`가 적용된 `SimpleFormatter` (기본 선택)
- `@MiniQualifier("markdown")`이 적용된 `MarkdownFormatter` (특정 선택)
- `MemoService`에서 생성자와 필드 주입 혼용

**테스트 시나리오**:
- Primary Formatter가 기본으로 선택되는지 확인
- Qualifier로 특정 구현체 선택 가능한지 확인
- 생성자와 필드 주입이 혼용되어 동작하는지 확인

### 2. setter_injection_app
**목적**: 세터 주입 방식 테스트
- `@MiniPrimary`가 적용된 `ConsoleLogger` (기본 선택)
- `@MiniQualifier("file")`이 적용된 `FileLogger` (특정 선택)
- `UserService`에서 세터 주입 사용

**테스트 시나리오**:
- 세터 메서드에 `@MiniAutowired` 적용 가능한지 확인
- 세터 메서드 파라미터에 `@MiniQualifier` 적용 가능한지 확인
- Primary와 Qualifier 우선순위가 올바른지 확인

### 3. error_cases_app
**목적**: 에러 케이스 테스트
- 중복 `@MiniPrimary` 사용 (에러 발생 예정)
- 존재하지 않는 `@MiniQualifier("nonexistent")` 사용 (에러 발생 예정)

**테스트 시나리오**:
- `@MiniPrimary`가 둘 이상이면 예외 발생하는지 확인
- 존재하지 않는 Qualifier 사용 시 명확한 예외 발생하는지 확인

## 성공 기준 (DoD)

- [x] 여러 후보 중 `@MiniPrimary`가 있으면 기본으로 선택된다.
- [x] `@MiniQualifier("name")`로 특정 구현을 선택할 수 있다.
- [x] 생성자/필드/세터 주입 모두 지원된다.
- [x] 주입 우선순위: Qualifier > Primary > 단일 후보 > 모호성 예외
- [x] 존재하지 않는 Qualifier 사용 시 명확한 예외 발생
- [x] `@MiniPrimary`가 둘 이상이면 예외 발생
- [x] 1주차 예시 그대로 실행 가능 (하위 호환)

## 실행 방법

각 앱 디렉토리에서 다음 명령어로 실행:

```bash
# 1. 기본 DI 테스트
cd primary_qualifier_app
javac -cp "../minispring" *.java
java -cp ".:../minispring" Main

# 2. 세터 주입 테스트
cd ../setter_injection_app
javac -cp "../minispring" *.java
java -cp ".:../minispring" Main

# 3. 에러 케이스 테스트
cd ../error_cases_app
javac -cp "../minispring" *.java
java -cp ".:../minispring" Main
```

## 예상 결과

### primary_qualifier_app
- Primary Formatter가 기본으로 선택됨
- MemoService에서 Markdown Formatter가 주입됨
- Clock이 필드 주입으로 주입됨

### setter_injection_app
- Primary Logger가 기본으로 선택됨
- UserService에서 세터 주입이 정상 동작
- File Logger가 별도로 주입됨

### error_cases_app
- 중복 Primary로 인한 예외 발생
- 존재하지 않는 Qualifier로 인한 예외 발생

## 구현해야 할 기능

현재 minispring에는 다음 기능들이 구현되어야 합니다:

1. `@MiniPrimary` 어노테이션 지원
2. `@MiniQualifier` 어노테이션 지원
3. 생성자 주입 지원 (현재 필드 주입만 지원)
4. 세터 주입 지원
5. 주입 우선순위 로직 구현
6. 에러 처리 로직 구현 