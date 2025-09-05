package com.example.chatbot.member.application;

import com.example.chatbot.member.infrastructure.persistence.jpa.MemberJpaRepository;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MemberService {

    private final MemberJpaRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberJpaRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원 가입 처리(간단 파라미터 버전)
     */
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
