# 🐛 LazyInitializationException 트러블슈팅 가이드

## 🚨 오류 내용
```
Caused by: org.hibernate.LazyInitializationException: 
failed to lazily initialize a collection of role: 
com.example.chatbot.auth.infrastructure.persistence.jpa.entity.AuthorityJpaEntity.roles: 
could not initialize proxy - no Session
```

## 📍 문제가 되는 코드 위치

### 1. 오류 발생 파일
**파일**: `src/main/java/com/example/chatbot/config/DataInitializer.java`
**메소드**: `assignAuthoritiesToRoles()`
**라인**: 약 60라인 근처

```java
private void assignAuthoritiesToRoles() {
    log.info("역할별 권한 할당...");

    // ADMIN 역할: 모든 권한
    RoleJpaEntity adminRole = roleRepository.findByNameWithAuthorities("ADMIN").orElse(null);
    if (adminRole != null) {
        AuthorityJpaEntity manageUsers = authorityRepository.findByName("MANAGE_USERS").orElse(null);
        AuthorityJpaEntity manageSystem = authorityRepository.findByName("MANAGE_SYSTEM").orElse(null);

        if (manageUsers != null && !adminRole.getAuthorities().contains(manageUsers)) {
            adminRole.addAuthority(manageUsers); // ❌ 여기서 오류 발생
            log.info("ADMIN 역할에 MANAGE_USERS 권한 추가");
        }
        // ... 생략
    }
}
```

### 2. 관련 엔티티 파일들
**파일**: `src/main/java/com/example/chatbot/auth/infrastructure/persistence/jpa/entity/RoleJpaEntity.java`
**메소드**: `addAuthority()`
```java
public void addAuthority(AuthorityJpaEntity authority) {
    this.authorities.add(authority);
    authority.getRoles().add(this); // ❌ 여기서 LazyInitializationException 발생
}
```

**파일**: `src/main/java/com/example/chatbot/auth/infrastructure/persistence/jpa/entity/AuthorityJpaEntity.java`
**필드**: `roles`
```java
@ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY) // ❌ 지연 로딩 설정
private Set<RoleJpaEntity> roles = new HashSet<>();
```

## 🔍 문제 원인 분석

### 1. @PostConstruct + @Transactional 조합 문제
```java
@Component
public class DataInitializer {
    
    @PostConstruct
    @Transactional  // ❌ @PostConstruct와 함께 사용 시 트랜잭션이 제대로 작동하지 않을 수 있음
    public void initializeData() {
        // ...
    }
}
```

**문제점**:
- `@PostConstruct`는 Spring Bean 초기화 단계에서 실행
- 이 시점에서 `@Transactional` 프록시가 완전히 준비되지 않을 수 있음
- 트랜잭션이 제대로 시작되지 않아 Hibernate Session이 없는 상태

### 2. 양방향 매핑에서의 지연 로딩 접근
```java
// RoleJpaEntity.addAuthority() 메소드에서
authority.getRoles().add(this); 
```
- `AuthorityJpaEntity.roles`가 `@ManyToMany(fetch = FetchType.LAZY)`로 설정됨
- 트랜잭션이 없는 상태에서 지연 로딩 컬렉션에 접근하려고 시도
- Hibernate Session이 없어서 LazyInitializationException 발생

## 🛠️ 해결방법들

### 해결방법 1: ApplicationRunner 사용 (추천)
**수정할 파일**: `src/main/java/com/example/chatbot/config/DataInitializer.java`

```java
@Component
public class DataInitializer implements ApplicationRunner {

    // ... 기존 필드들

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("데이터 초기화 시작...");
        
        createAuthoritiesIfNotExists();
        createRolesIfNotExists();
        assignAuthoritiesToRoles();
        
        log.info("데이터 초기화 완료!");
    }

    // ... 나머지 메소드들은 동일
}
```

**장점**:
- 애플리케이션이 완전히 시작된 후 실행
- `@Transactional`이 확실히 작동
- Spring 컨텍스트가 완전히 준비된 상태

### 해결방법 2: @EventListener 사용
```java
@Component
public class DataInitializer {
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        // ... 초기화 로직
    }
}
```

### 해결방법 3: 양방향 매핑 제거 (임시방편)
**수정할 파일**: `src/main/java/com/example/chatbot/auth/infrastructure/persistence/jpa/entity/RoleJpaEntity.java`

```java
public void addAuthority(AuthorityJpaEntity authority) {
    this.authorities.add(authority);
    // authority.getRoles().add(this); // 주석 처리하여 LazyInitializationException 회피
}
```

**단점**: 양방향 매핑의 일관성 깨짐

### 해결방법 4: EAGER 로딩 사용 (비추천)
```java
@ManyToMany(mappedBy = "authorities", fetch = FetchType.EAGER)
private Set<RoleJpaEntity> roles = new HashSet<>();
```

**단점**: N+1 쿼리 문제, 성능 이슈

## 🎯 권장 해결방법

**1단계**: `DataInitializer`를 `ApplicationRunner`로 변경
**2단계**: `@PostConstruct` 제거
**3단계**: 애플리케이션 재시작 및 테스트

## 🔍 디버깅 팁

### 로그 확인
```yaml
# application.yml에 추가
logging:
  level:
    org.springframework.transaction: DEBUG
    org.hibernate.SQL: DEBUG
    com.example.chatbot.config.DataInitializer: DEBUG
```

### 트랜잭션 상태 확인
```java
@Override
@Transactional
public void run(ApplicationArguments args) throws Exception {
    log.info("트랜잭션 활성 상태: {}", TransactionSynchronizationManager.isActualTransactionActive());
    // ... 초기화 로직
}
```

## 📚 관련 문서
- [Spring @PostConstruct vs ApplicationRunner](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-postconstruct-and-predestroy-annotations)
- [Hibernate LazyInitializationException 해결방법](https://hibernate.org/community/compatibility/)
- [Spring @Transactional 동작 원리](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)