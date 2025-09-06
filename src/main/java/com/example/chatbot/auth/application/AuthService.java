package com.example.chatbot.auth.application;

import com.example.chatbot.auth.infrastructure.JwtTokenService;
import com.example.chatbot.member.infrastructure.persistence.jpa.MemberJpaRepository;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final MemberJpaRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(MemberJpaRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 로그인 처리 및 JWT 토큰 생성
     */
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

    /**
     * 사용자 정보 조회 (인증 후)
     */
    @Transactional(readOnly = true)
    public MemberJpaEntity findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}