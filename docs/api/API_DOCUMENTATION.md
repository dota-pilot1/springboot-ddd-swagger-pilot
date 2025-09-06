# API 문서화 가이드

## Swagger UI 접근 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. Swagger UI 접근
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## SpringDoc OpenAPI vs Swagger

### SpringDoc OpenAPI
- **SpringDoc OpenAPI**는 Spring Boot 3.x와 호환되는 최신 OpenAPI 3.0 스펙 구현체입니다.
- 기존의 Springfox Swagger를 대체하는 솔루션으로, Spring Boot 3.x에서 권장됩니다.

#### 주요 특징:
- OpenAPI 3.0 스펙 완전 지원
- Spring Boot 3.x Native 지원
- 자동 API 문서 생성
- Interactive UI 제공
- JSON/YAML 형식 지원

### Swagger vs SpringDoc 비교

| 항목 | Springfox Swagger | SpringDoc OpenAPI |
|------|------------------|-------------------|
| Spring Boot 3.x 지원 | ❌ | ✅ |
| OpenAPI 3.0 | 부분 지원 | 완전 지원 |
| 유지보수 | 중단됨 | 활발함 |
| 성능 | 상대적으로 느림 | 빠름 |
| 설정 복잡도 | 높음 | 낮음 |

## 현재 프로젝트 설정

### build.gradle 의존성
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0'
```

### 기본 설정 (application.yml 추가 가능)
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  api-docs:
    path: /v3/api-docs
    enabled: true
```

## API 문서화 어노테이션 예시

### Controller 예시
```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
public class AuthController {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/signup")
    public ResponseEntity<MemberSignupResponse> signup(
        @RequestBody @Valid @Parameter(description = "회원가입 정보") MemberSignupRequest request
    ) {
        // 구현 코드
    }
}
```

### DTO 예시
```java
@Schema(description = "회원가입 요청")
public class MemberSignupRequest {
    
    @Schema(description = "사용자 이름", example = "john_doe", required = true)
    @NotBlank
    private String username;
    
    @Schema(description = "비밀번호", example = "password123", required = true)
    @NotBlank
    private String password;
}
```

## 참고 자료

### SpringDoc OpenAPI
- [SpringDoc OpenAPI 공식 문서](https://springdoc.org/)
- [SpringDoc OpenAPI GitHub](https://github.com/springdoc/springdoc-openapi)
- [SpringDoc OpenAPI v2 마이그레이션 가이드](https://springdoc.org/v2/)

### OpenAPI 3.0 스펙
- [OpenAPI 3.0 스펙](https://spec.openapis.org/oas/v3.0.3/)
- [Swagger Editor](https://editor.swagger.io/)

### Spring Boot 관련
- [Spring Boot 3.x 문서](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Boot REST API 베스트 프랙티스](https://spring.io/guides/gs/rest-service/)

### 추가 도구
- [Postman](https://www.postman.com/) - API 테스팅
- [Insomnia](https://insomnia.rest/) - API 클라이언트
- [OpenAPI Generator](https://openapi-generator.tech/) - 클라이언트 코드 생성