package com.example.chatbot.member.interfaces.api;

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

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        MemberJpaEntity saved = memberService.signup(request.getEmail(), request.getPassword(), request.getName());
        MemberSignupResponse resp = new MemberSignupResponse(saved.getId(), saved.getEmail(), saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
