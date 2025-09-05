# Swagger(OpenAPI) 연동 가이드

## 지금 설정 요약
- 의존성: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- 추가 코드 설정 없음(기본 자동 구성). UI/스펙 경로는 이미 SecurityConfig에서 permitAll.
- 접속 경로
  - UI: http://localhost:8080/swagger-ui/index.html
  - JSON: http://localhost:8080/v3/api-docs

## 어떻게 컨트롤러/DTO와 연결되나
- 대상: `@RestController` + `@RequestMapping/@GetMapping/@PostMapping` 등으로 매핑된 핸들러 메서드
- 원리: springdoc이 스프링 MVC 핸들러 맵핑을 스캔해 Operation을 생성하고, 요청/응답 타입을 바탕으로 스키마를 생성
- DTO 스키마: 요청/응답 DTO의 필드, Bean Validation(@NotBlank, @Email, @Size 등) 메타데이터가 스키마 제약으로 반영됨
- 서비스 계층: 문서화 대상이 아님(문서는 Web 레이어 기준). 서비스는 컨트롤러 내부 구현 세부로 취급됨

## 최소 사용법(주석만 추가하면 됨)
컨트롤러 메서드 위에 메타데이터 주석을 붙여 문서를 풍부하게 할 수 있음.
- `@Operation(summary, description, tags)`
- `@ApiResponses`, `@ApiResponse`, `@Content`, `@Schema`
- `@Parameter`, `@RequestBody`(설명 추가 시)

예시(요약)
```java
@Operation(summary = "회원가입", description = "이메일/비밀번호/이름으로 회원을 생성")
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "생성됨"),
  @ApiResponse(responseCode = "400", description = "검증 실패"),
  @ApiResponse(responseCode = "409", description = "이메일 중복")
})
@PostMapping("/api/auth/signup")
public ResponseEntity<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest req) { ... }
```

DTO 예시(필드 설명)
```java
public class MemberSignupRequest {
  @Schema(description = "로그인 이메일", example = "user@example.com")
  @Email @NotBlank String email;
  @Schema(description = "비밀번호", minLength = 6, maxLength = 100)
  @NotBlank @Size(min=6, max=100) String password;
  @Schema(description = "표시 이름", example = "User")
  @NotBlank String name;
}
```

## 그룹화/패키지 제한(선택)
엔드포인트가 많아지면 GroupedOpenApi로 묶을 수 있음.
```java
@Configuration
class OpenApiConfig {
  @Bean
  GroupedOpenApi authApi() {
    return GroupedOpenApi.builder()
      .group("auth")
      .pathsToMatch("/api/auth/**")
      .build();
  }
}
```
- 여러 그룹 생성 가능: `member`, `admin`, `public` 등
- 특정 패키지만 스캔하고 싶으면 `packagesToScan` 사용

## 엔드포인트 제외/숨김
- 주석으로 숨김: `@Hidden`
- 패턴으로 제외: `springdoc.paths-to-exclude=/actuator/**,/internal/**` (application.yml)

## 보안 스키마(차후 JWT 등 사용 시)
JWT/Bearer 인증을 문서에 반영하려면 보안 스키마를 선언하고 메서드에 적용.
```java
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
class OpenApiSecurity {}

@Operation(summary = "보호된 API", security = @SecurityRequirement(name = "bearerAuth"))
@GetMapping("/api/me")
public UserDto me() { ... }
```

## 권장 컨벤션
- 필수 엔드포인트에만 `@Operation(summary)` 우선 적용 → 점진 개선
- DTO 필드에 `@Schema(description, example)`로 샘플 제공
- 그룹으로 큰 영역을 나누고, 태그(tags)로 세부 카테고리화

## 문제 해결
- 엔드포인트가 UI에 안 보임: 컨트롤러가 `@RestController`인지, 경로가 보안에서 허용되는지 확인
- DTO 스키마가 이상함: Lombok 접근자 여부, 제네릭/내부 클래스 사용, Validation 애노테이션 존재 여부 점검
- UI 404: 의존성 누락 또는 경로 오타(`/swagger-ui/index.html`)

