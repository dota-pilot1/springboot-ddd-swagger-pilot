# 🏗️ DDD 기반 패키지 구조

## 개요
이 프로젝트는 Domain Driven Design(DDD) 원칙을 따라 설계되었습니다.

## 📦 전체 패키지 구조

```
src/main/java/com/example/chatbot/
├── auth/                           # 인증 도메인 (Bounded Context)
│   ├── application/                # Application Layer
│   │   └── AuthService.java        # 인증 비즈니스 로직
│   ├── infrastructure/             # Infrastructure Layer
│   │   └── JwtTokenService.java    # JWT 토큰 처리 (기술적 구현)
│   └── interfaces/                 # Interface Layer
│       ├── api/
│       │   └── AuthController.java # REST API 엔드포인트
│       └── dto/
│           ├── LoginRequest.java
│           └── LoginResponse.java
├── member/                         # 회원 도메인 (Bounded Context)
│   ├── application/                # Application Layer
│   │   └── MemberService.java      # 회원 비즈니스 로직
│   ├── infrastructure/             # Infrastructure Layer
│   │   ├── persistence/
│   │   │   └── jpa/
│   │   │       ├── MemberJpaRepository.java
│   │   │       └── entity/
│   │   │           └── MemberJpaEntity.java
│   │   └── security/
│   │       └── MemberUserDetailsService.java
│   └── interfaces/                 # Interface Layer
│       └── api/
│           └── dto/
│               ├── MemberSignupRequest.java
│               └── MemberSignupResponse.java
├── config/                         # 공통 설정
│   └── SecurityConfig.java
└── ChatbotServiceApplication.java  # Main Application
```

## 🎯 DDD 레이어별 설명

### 1. Domain Layer (도메인 계층)
**위치**: 각 도메인의 핵심 비즈니스 로직
- **엔티티(Entity)**: 비즈니스 식별자를 가진 객체
- **값 객체(Value Object)**: 식별자가 없는 불변 객체
- **도메인 서비스**: 여러 엔티티에 걸친 비즈니스 로직

### 2. Application Layer (응용 계층)
**위치**: `*/application/`
- **역할**: 비즈니스 유스케이스 조율
- **특징**: 도메인 로직을 사용하여 애플리케이션 서비스 제공
- **예시**: `AuthService`, `MemberService`

### 3. Infrastructure Layer (인프라 계층)
**위치**: `*/infrastructure/`
- **역할**: 외부 시스템과의 연동 (DB, 외부 API 등)
- **특징**: 기술적 구현 세부사항 포함
- **예시**: `JwtTokenService`, `MemberJpaRepository`

### 4. Interface Layer (인터페이스 계층)
**위치**: `*/interfaces/`
- **역할**: 외부와의 통신 (REST API, 웹 인터페이스 등)
- **특징**: 요청/응답 변환, 유효성 검증
- **예시**: `AuthController`, DTO 클래스들

## 🔄 도메인 간 협력

### Auth ↔ Member 도메인 협력
```java
// AuthService에서 Member 도메인의 Repository 사용
@Service
public class AuthService {
    private final MemberJpaRepository memberRepository; // Member 도메인 접근
    
    public String login(String email, String password) {
        // Member 도메인의 데이터에 접근하여 인증 처리
        MemberJpaEntity member = memberRepository.findByEmail(email)...
    }
}
```

**협력 원칙**:
- Repository를 통한 간접 접근
- 직접적인 도메인 객체 참조 최소화
- 향후 도메인 서비스 또는 이벤트 기반으로 발전 가능

## 📋 각 도메인별 책임

### 🔐 Auth 도메인
**핵심 책임**: 인증 및 권한 관리
- 로그인/로그아웃 처리
- JWT 토큰 생성/검증
- 사용자 인증 상태 관리
- 권한 검증

**확장 가능성**:
- OAuth 소셜 로그인
- 2FA(Two-Factor Authentication)
- 세션 관리
- 권한 기반 접근 제어(RBAC)

### 👤 Member 도메인  
**핵심 책임**: 회원 정보 관리
- 회원가입 처리
- 프로필 정보 관리
- 회원 데이터 CRUD

**확장 가능성**:
- 프로필 이미지 관리
- 회원 등급 시스템
- 친구/팔로우 관계
- 회원 통계 및 분석

## 🎯 DDD 적용의 장점

### 1. **명확한 책임 분리**
- 각 도메인이 고유한 비즈니스 영역 담당
- 코드 수정 시 영향 범위 명확

### 2. **높은 응집도**
- 관련된 코드가 같은 패키지에 위치
- 비즈니스 로직이 도메인별로 집중

### 3. **낮은 결합도**
- 도메인 간 인터페이스를 통한 통신
- 한 도메인의 변경이 다른 도메인에 미치는 영향 최소화

### 4. **확장 용이성**
- 새로운 도메인 추가 시 기존 구조 활용
- 각 도메인별 독립적인 발전 가능

### 5. **테스트 용이성**
- 도메인별 단위 테스트 작성 용이
- Mock 객체 활용이 명확

## 🚀 향후 확장 계획

### 새로운 도메인 추가 예시
```
chat/                              # 채팅 도메인
├── application/
│   ├── ChatService.java
│   └── MessageService.java
├── infrastructure/
│   ├── websocket/
│   └── persistence/
└── interfaces/
    ├── websocket/
    └── dto/

notification/                      # 알림 도메인
├── application/
├── infrastructure/
└── interfaces/
```

## 💡 베스트 프랙티스

### 1. 패키지 네이밍
- 도메인명은 비즈니스 용어 사용
- 기술적 용어보다는 업무 도메인 용어 우선

### 2. 의존성 방향
```
Interface → Application → Domain ← Infrastructure
```

### 3. 도메인 간 통신
- 직접 참조보다는 이벤트 기반 통신 고려
- 필요시 Domain Service 활용

### 4. 공통 코드
- 여러 도메인에서 사용되는 코드는 `common` 패키지 고려
- 설정 관련 코드는 `config` 패키지에 위치

---

이 구조를 통해 확장 가능하고 유지보수가 용이한 백엔드 시스템을 구축할 수 있습니다.