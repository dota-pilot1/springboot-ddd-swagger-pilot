package com.example.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**"
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable());
        return http.build();
    }

    // SecurityFilterChain 빈은 스프링 시큐리티의 HTTP 보안 설정을 담당합니다.
    // 이 메서드에서 CSRF, 세션 정책, 인증/인가 규칙, 인증 방식 등을 설정합니다.
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화: API 서버(특히 토큰 기반 인증)나 WebSocket을 사용하는 경우 기본적으로 비활성화할 수 있음
            .csrf(csrf -> csrf.disable())

            // 세션 정책을 STATELESS로 설정: 서버에서 세션을 유지하지 않고 토큰 기반 인증을 전제로 함
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 요청별 접근 제어 설정
            .authorizeHttpRequests(auth -> auth
                // permitAll로 지정한 경로는 인증 없이 접근 허용
                .requestMatchers(
                    "/actuator/**",          // 헬스체크/메트릭 등
                    "/favicon.ico",
                    "/",
                    "/index.html",
                    "/static/**",
                    "/ws/**",               // WebSocket 엔드포인트(공개인 경우)
                    "/stomp/**",            // STOMP 관련 경로
                    "/api/auth/**"          // 인증 관련(회원가입/로그인) 공개 엔드포인트
                ).permitAll()
                // 위에 해당하지 않는 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // 간단하게 HTTP Basic 인증을 사용하도록 설정(개발/테스트용)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // PasswordEncoder 빈: 비밀번호를 안전하게 해시하기 위해 BCrypt 사용
    // 실제 운영 환경에서는 비밀번호 ��책/인코더를 상황에 맞게 조정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
