# 🛠️ 개발 환경 설정 가이드

## 📋 필요한 소프트웨어

### 필수 요구사항
- **Java 21** - OpenJDK 또는 Oracle JDK
- **PostgreSQL 15+** - 데이터베이스
- **Redis 7+** - 세션 및 캐시 (선택적)
- **Git** - 버전 관리

### 권장 개발 도구
- **IntelliJ IDEA** - IDE (Community/Ultimate)
- **Docker & Docker Compose** - 컨테이너 환경
- **Postman** - API 테스트 (또는 Swagger UI 사용)

## 🚀 빠른 시작

### 1. 저장소 클론
```bash
git clone <repository-url>
cd callbot-backend
```

### 2. 데이터베이스 설정

#### PostgreSQL 설치 및 설정
```bash
# macOS (Homebrew)
brew install postgresql
brew services start postgresql

# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Docker 사용 (추천)
docker run -d \
  --name postgres-chatbot \
  -e POSTGRES_DB=chatdb \
  -e POSTGRES_USER=chatuser \
  -e POSTGRES_PASSWORD=chatpass \
  -p 5432:5432 \
  postgres:15
```

#### 데이터베이스 생성
```sql
-- PostgreSQL CLI에서 실행
CREATE DATABASE chatdb;
CREATE USER chatuser WITH PASSWORD 'chatpass';
GRANT ALL PRIVILEGES ON DATABASE chatdb TO chatuser;
```

### 3. Redis 설정 (선택적)
```bash
# Docker 사용
docker run -d \
  --name redis-chatbot \
  -p 6379:6379 \
  redis:7-alpine

# macOS (Homebrew)
brew install redis
brew services start redis
```

### 4. 애플리케이션 설정

#### application.yml 확인
```yaml
# src/main/resources/application.yml
spring:
  application:
    name: chatbot-service
  datasource:
    url: jdbc:postgresql://localhost:5432/chatdb
    username: chatuser
    password: chatpass
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # 개발환경에서만 사용
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  data:
    redis:
      host: localhost
      port: 6379

# JWT 설정 (선택적 - 기본값 사용 가능)
jwt:
  secret: mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256
  expiration: 86400000  # 24시간 (milliseconds)

logging:
  level:
    org.springframework.web.socket: DEBUG
    org.springframework.messaging: DEBUG
    com.example.chatbot: DEBUG
```

### 5. 애플리케이션 실행
```bash
# Gradle 사용
./gradlew bootRun

# 또는 IDE에서 ChatbotServiceApplication.java 실행
```

## 🔧 개발 환경 설정

### IntelliJ IDEA 설정

#### 1. 프로젝트 열기
- `File` → `Open` → 프로젝트 폴더 선택
- Gradle 프로젝트로 인식되면 자동 import

#### 2. JDK 설정
- `File` → `Project Structure` → `Project`
- Project SDK를 Java 21로 설정
- Project language level을 21로 설정

#### 3. Gradle 설정
- `File` → `Settings` → `Build, Execution, Deployment` → `Gradle`
- Build and run using: `Gradle`
- Run tests using: `Gradle`

#### 4. 유용한 플러그인
- **Lombok** - getter/setter 자동 생성
- **JPA Buddy** - JPA 엔티티 관리
- **Database Navigator** - 데이터베이스 관리

### VS Code 설정 (선택적)

#### 필수 확장
```bash
# Extension Pack for Java
# Spring Boot Extension Pack
# PostgreSQL (cweijan.vscode-postgresql-client2)
```

## 📊 데이터베이스 초기 데이터

### 테스트 사용자 생성 (선택적)
```sql
-- 개발용 테스트 데이터
INSERT INTO members (email, password, name, role, created_at) VALUES 
('admin@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin User', 'ROLE_ADMIN', NOW()),
('user@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test User', 'ROLE_USER', NOW());

-- 비밀번호는 'secret' (BCrypt 해시)
```

## 🧪 애플리케이션 테스트

### 1. 헬스 체크
```bash
curl http://localhost:8080/actuator/health
```

### 2. Swagger UI 접속
```
http://localhost:8080/swagger-ui/index.html
```

### 3. API 테스트
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 🐳 Docker Compose 사용 (추천)

### docker-compose.yml 생성
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    container_name: chatbot-postgres
    environment:
      POSTGRES_DB: chatdb
      POSTGRES_USER: chatuser
      POSTGRES_PASSWORD: chatpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: chatbot-redis
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### 실행
```bash
# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose down

# 데이터 포함 완전 제거
docker-compose down -v
```

## 🔍 트러블슈팅

### 1. 포트 충돌
```bash
# 포트 사용 확인
lsof -i :8080  # Spring Boot
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis

# 프로세스 종료
kill -9 <PID>
```

### 2. 데이터베이스 연결 오류
- PostgreSQL 서비스가 실행 중인지 확인
- 데이터베이스 사용자 권한 확인
- application.yml의 설정값 확인

### 3. Gradle 빌드 오류
```bash
# Gradle Wrapper 권한 설정
chmod +x gradlew

# 의존성 새로고침
./gradlew --refresh-dependencies

# Clean build
./gradlew clean build
```

### 4. JPA/Hibernate 오류
- Entity 클래스의 어노테이션 확인
- 데이터베이스 스키마와 Entity 매핑 확인
- `ddl-auto: validate` 사용 시 스키마 일치 확인

## 📝 개발 팁

### 1. 로그 설정
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG                    # SQL 쿼리 출력
    org.hibernate.type.descriptor.sql: TRACE   # 바인딩 파라미터 출력
    org.springframework.security: DEBUG        # Security 디버그
    com.example.chatbot: DEBUG                 # 애플리케이션 로그
```

### 2. 프로파일 활용
```yaml
# application-dev.yml (개발 환경)
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# application-prod.yml (운영 환경)  
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

```bash
# 프로파일 지정 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. 핫 리로드 설정
```yaml
# application.yml에 추가
spring:
  devtools:
    livereload:
      enabled: true
    restart:
      enabled: true
```

---

이 가이드를 따라하면 개발 환경을 성공적으로 구축할 수 있습니다. 추가 도움이 필요하면 팀에 문의하세요! 🚀