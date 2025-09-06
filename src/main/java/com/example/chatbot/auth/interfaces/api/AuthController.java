package com.example.chatbot.auth.interfaces.api;

import com.example.chatbot.auth.application.AuthService;
import com.example.chatbot.auth.interfaces.dto.LoginRequest;
import com.example.chatbot.auth.interfaces.dto.LoginResponse;
import com.example.chatbot.member.application.MemberService;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import com.example.chatbot.member.interfaces.api.dto.MemberSignupRequest;
import com.example.chatbot.member.interfaces.api.dto.MemberSignupResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
