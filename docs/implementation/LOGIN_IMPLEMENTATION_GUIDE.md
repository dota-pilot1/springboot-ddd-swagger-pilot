# 로그인 기능 구현 가이드

## 개요
Spring Boot 3.x, JWT, Spring Security를 활용하여 회원가입/로그인 기능을 구현한 단계별 가이드입니다.

---

## Step 1: 프로젝트 기본 설정

### 1.1 의존성 추가 (build.gradle)
```gradle
dependencies {
    // 기본 Spring Boot 의존성
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // JWT 의존성 - JJWT 0.12.x 사용
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // 기타
    implementation 'org.postgresql:postgresql'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### 1.2 데이터베이스 설정 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatdb
    username: chatuser
    password: chatpass
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

---

## Step 2: 도메인 모델 설계

### 2.1 Member 엔티티 (DDD 기반 구조)
```java
// src/main/java/com/example/chatbot/member/infrastructure/persistence/jpa/entity/MemberJpaEntity.java
@Entity
@Table(name = "members")
public class MemberJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String name;
    private String role;
    private LocalDateTime createdAt;
    
    // 생성자, getter, setter...
}
```

### 2.2 Repository 인터페이스
```java
// src/main/java/com/example/chatbot/member/infrastructure/persistence/jpa/MemberJpaRepository.java
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## Step 3: 보안 설정

### 3.1 Spring Security 기본 설정
```java
// src/main/java/com/example/chatbot/config/SecurityConfig.java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // 인증 API는 공개
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger 공개
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Step 4: JWT 토큰 서비스 구현

### 4.1 JWT 토큰 서비스
```java
// src/main/java/com/example/chatbot/auth/infrastructure/JwtTokenService.java
@Component
public class JwtTokenService {

    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24시간
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    // 기타 토큰 검증 메소드들...
}
```

---

## Step 5: 도메인 서비스 구현

### 5.1 회원 서비스 (회원가입 담당)
```java
// src/main/java/com/example/chatbot/member/application/MemberService.java
@Service
@Transactional
public class MemberService {

    private final MemberJpaRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberJpaRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberJpaEntity signup(String email, String rawPassword, String name) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        MemberJpaEntity entity = new MemberJpaEntity(
                null,
                email,
                passwordEncoder.encode(rawPassword),
                name,
                "ROLE_USER",
                LocalDateTime.now()
        );

        return memberRepository.save(entity);
    }
}
```

### 5.2 인증 서비스 (로그인 담당)
```java
// src/main/java/com/example/chatbot/auth/application/AuthService.java
@Service
@Transactional
public class AuthService {

    private final MemberJpaRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(MemberJpaRepository memberRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtTokenService jwtTokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        Optional<MemberJpaEntity> memberOpt = memberRepository.findByEmail(email);
        
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }

        MemberJpaEntity member = memberOpt.get();
        
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenService.generateToken(member.getEmail());
    }

    @Transactional(readOnly = true)
    public MemberJpaEntity findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
```

---

## Step 6: DTO 클래스 설계

### 6.1 로그인 요청 DTO
```java
// src/main/java/com/example/chatbot/auth/interfaces/dto/LoginRequest.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
```

### 6.2 로그인 응답 DTO
```java
// src/main/java/com/example/chatbot/auth/interfaces/dto/LoginResponse.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long memberId;
    private String email;
    private String name;

    public LoginResponse(String accessToken, Long memberId, String email, String name) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.memberId = memberId;
        this.email = email;
        this.name = name;
    }
}
```

---

## Step 7: REST API 컨트롤러

### 7.1 인증 컨트롤러
```java
// src/main/java/com/example/chatbot/auth/interfaces/api/AuthController.java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final AuthService authService;

    public AuthController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        MemberJpaEntity saved = memberService.signup(request.getEmail(), request.getPassword(), request.getName());
        MemberSignupResponse resp = new MemberSignupResponse(saved.getId(), saved.getEmail(), saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            MemberJpaEntity member = authService.findMemberByEmail(request.getEmail());
            
            LoginResponse response = new LoginResponse(
                token,
                member.getId(),
                member.getEmail(),
                member.getName()
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
```

---

## Step 8: 아키텍처 패턴 - DDD 기반 패키지 구조

```
src/main/java/com/example/chatbot/
├── auth/                           # 인증 도메인
│   ├── application/
│   │   └── AuthService.java        # 인증 비즈니스 로직
│   ├── infrastructure/
│   │   └── JwtTokenService.java    # JWT 토큰 처리
│   └── interfaces/
│       ├── api/
│       │   └── AuthController.java # 인증 API
│       └── dto/
│           ├── LoginRequest.java
│           └── LoginResponse.java
├── member/                         # 회원 도메인
│   ├── application/
│   │   └── MemberService.java      # 회원 비즈니스 로직
│   ├── infrastructure/
│   │   └── persistence/
│   │       └── jpa/
│   │           ├── MemberJpaRepository.java
│   │           └── entity/
│   │               └── MemberJpaEntity.java
│   └── interfaces/
│       └── api/
│           └── dto/
│               ├── MemberSignupRequest.java
│               └── MemberSignupResponse.java
└── config/
    └── SecurityConfig.java         # 보안 설정
```

---

## Step 9: API 테스트

### 9.1 Swagger 설정
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0'
```

### 9.2 회원가입 API 테스트
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

### 9.3 로그인 API 테스트
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5...",
  "tokenType": "Bearer",
  "memberId": 1,
  "email": "test@example.com",
  "name": "홍길동"
}
```

---

## Step 10: 주요 구현 포인트

### 10.1 보안 고려사항
- 비밀번호는 BCrypt로 해싱하여 저장
- JWT 시크릿 키는 충분히 길고 안전한 값 사용
- CSRF 비활성화 (토큰 기반 인증 시)
- 세션 정책을 STATELESS로 설정

### 10.2 에러 처리
- 로그인 실패 시 구체적인 실패 이유 노출 방지 (보안)
- 401 Unauthorized 상태코드 반환
- ValidationException은 400 Bad Request 처리

### 10.3 DDD 아키텍처의 장점
- **도메인 분리**: Auth와 Member 도메인의 명확한 책임 분리
- **확장성**: 각 도메인별 독립적 확장 가능
- **유지보수**: 비즈니스 로직이 도메인별로 집중됨

---

## 결론

이 구현은 다음과 같은 특징을 가집니다:

1. **Modern Stack**: Spring Boot 3.x + JWT + Spring Security
2. **Clean Architecture**: DDD 기반 패키지 구조
3. **Security First**: 보안 모범 사례 적용
4. **API First**: RESTful API 설계
5. **Documentation**: Swagger를 통한 API 문서화

이 가이드를 통해 확장 가능하고 안전한 인증 시스템을 구현할 수 있습니다.