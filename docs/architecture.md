# 아키텍처 개요

현재 구조는 레이어드(계층형) 아키텍처에 가깝고, DDD의 일부 분리 컨벤션을 참고합니다. 도메인 POJO/포트는 제거되어 JPA 단일화로 단순화되어 있습니다.

## 패키지 구조
- interfaces/api
  - REST 컨트롤러(AuthController, DemoErrorController)
  - 요청/응답 DTO (interfaces/api/dto)
- application
  - 유스케이스(Service): MemberService
- infrastructure/persistence/jpa
  - Repository: MemberJpaRepository (Spring Data JPA)
  - Entity: entity/MemberJpaEntity
- infrastructure/security
  - 스프링 시큐리티 어댑터: MemberUserDetailsService
- common/exception
  - 전역 에러 처리: GlobalExceptionHandler, ErrorResponse
- config
  - 보안 설정: SecurityConfig

## 요청 흐름(회원가입)
1) Controller: MemberSignupRequest(@Valid) 수신 → MemberService 호출
2) Service: 중복 검사, 비밀번호 인코딩, 엔티티 생성 → MemberJpaRepository.save
3) Repository: JPA로 DB 반영
4) Controller: MemberSignupResponse(요약 정보)로 201 Created 응답
5) Error: 예외 발생 시 GlobalExceptionHandler가 표준 ErrorResponse(JSON)로 응답

## 보안
- HTTP Basic(개발용) + Stateless
- 공개 경로: /api/auth/**, /actuator/** 등
- 나머지 인증 필요
- UserDetailsService는 infrastructure/security에 위치(프레임워크 어댑터 성격)

## 현 구조의 성격
- DDD 정석(도메인 모델/포트/어댑터 완전 분리)은 아님
- 단순/명료한 JPA 기반 계층 분리로 개발 속도와 가독성 우선

## 확장 가이드
- 복잡도가 증가하면 다음을 고려
  - 도메인 포트(인터페이스) 도입 → 인프라 어댑터(JPA, 외부 API) 분리
  - 값 객체(Email 등)와 도메인 규칙 캡슐화
  - JWT/OAuth2 인증 전환, 권한 정책 세분화
  - Flyway/Liquibase로 스키마 버전 관리
  - 통합 테스트 추가(회원가입 201, 검증 400, 중복 409)

## 테스트 팁(회원가입)
- Postman/curl: 현재 프로젝트는 springdoc 의존성이 없으므로 빠르게 호출/검증에 적합
  - POST /api/auth/signup, Content-Type: application/json
  - body: {"email":"user@example.com","password":"secret123","name":"User"}
- Swagger(OpenAPI) 사용 원할 시
  - springdoc-openapi-ui 의존성을 추가하고, SecurityConfig의 permitAll 경로(/swagger-ui/**, /v3/api-docs/**)는 이미 허용
  - 팀 내 API 탐색/공유가 중요하다면 Swagger를 권장

