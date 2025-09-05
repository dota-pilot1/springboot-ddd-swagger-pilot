# Chatbot Service

간단 요약
- Spring Boot 3, Spring Security, Spring Data JPA 기반
- JPA 단일화(도메인 POJO 제거): MemberJpaEntity + MemberJpaRepository 사용
- 전역 예외 처리(GlobalExceptionHandler)로 표준 오류 응답 제공

## 빌드 & 실행
```bash
./gradlew clean build -x test
./gradlew bootRun
```

환경 설정
- src/main/resources/application.yml에서 DB/Redis 설정
- spring.security.user 기본 계정은 제거됨(UserDetailsService 사용)

## API
- 회원가입: POST /api/auth/signup
  - Request
    - email: string (형식 검사)
    - password: string (6~100자)
    - name: string
  - Response (201)
    - { id, email, name }

예시 요청
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret123",
  "name": "User"
}
```

## 오류 응답(표준 형태)
- ErrorResponse
```
{
  "timestamp": "2025-09-06T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/signup",
  "errors": [
    { "field": "email", "rejectedValue": "bad", "reason": "must be a well-formed email address" }
  ]
}
```

## 보안
- HTTP Basic 사용(개발 기본)
- 공개 경로: /api/auth/**, /actuator/** 등
- 그 외 요청은 인증 필요

## 패키지 구조(요약)
- member
  - application: MemberService, command/MemberCreateCommand
  - infrastructure/persistence/jpa: MemberJpaRepository, entity/MemberJpaEntity
  - infrastructure/security: MemberUserDetailsService
  - interfaces/api: AuthController, dto
- common/exception: GlobalExceptionHandler, ErrorResponse

## 개발 메모
- 스키마 관리는 JPA ddl-auto=update (운영 전 Flyway/Liquibase 권장)
- 예외: DataIntegrityViolationException → 409(CONFLICT)

# springboot-ddd-swagger-pilot
