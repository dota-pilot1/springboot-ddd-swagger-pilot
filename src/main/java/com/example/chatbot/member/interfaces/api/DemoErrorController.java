package com.example.chatbot.member.interfaces.api;

import com.example.chatbot.member.interfaces.api.dto.MemberSignupRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth/demo")
public class DemoErrorController {

    // DTO 검증 실패(400) 데모: 잘못된 email/password를 보내면 MethodArgumentNotValidException 발생
    @PostMapping("/validation")
    public ResponseEntity<Void> validation(@Valid @RequestBody MemberSignupRequest request) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 잘못된 입력(400) 데모: 서비스에서 IllegalArgumentException을 던지는 상황 재현
    @GetMapping("/illegal-arg")
    public ResponseEntity<Void> illegalArg() {
        throw new IllegalArgumentException("잘못된 입력입니다.");
    }

    // 리소스 없음(404) 데모: 조회 실패 상황 재현
    @GetMapping("/not-found")
    public ResponseEntity<Void> notFound() {
        throw new NoSuchElementException("리소스를 찾을 수 없습니다.");
    }

    // 유니크 제약 위반 등 충돌(409) 데모
    @GetMapping("/conflict")
    public ResponseEntity<Void> conflict() {
        throw new DataIntegrityViolationException("이메일이 이미 사용 중입니다.");
    }

    // 미처리 예외(500) 데모
    @GetMapping("/boom")
    public ResponseEntity<Void> boom() {
        throw new RuntimeException("예상치 못한 오류");
    }
}

