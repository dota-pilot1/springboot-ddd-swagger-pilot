package com.example.chatbot.member.application;

import com.example.chatbot.auth.application.AuthorityService;
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
    private final AuthorityService authorityService;

    public MemberService(MemberJpaRepository memberRepository, 
                        PasswordEncoder passwordEncoder,
                        AuthorityService authorityService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityService = authorityService;
    }

    /**
     * 회원 가입 처리 - 기본 USER 역할 자동 할당
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
                LocalDateTime.now()
        );

        MemberJpaEntity savedMember = memberRepository.save(entity);
        
        // 기본 USER 역할 할당
        try {
            authorityService.assignRoleToMember(savedMember.getId(), "USER", null);
        } catch (Exception e) {
            // 역할 할당 실패 시 로그만 남기고 계속 진행 (회원가입은 성공)
            // 실제로는 더 정교한 오류 처리가 필요할 수 있음
            System.err.println("기본 역할 할당 실패: " + e.getMessage());
        }

        return savedMember;
    }

    /**
     * 관리자 계정 생성 (개발/초기화 용도)
     */
    public MemberJpaEntity createAdmin(String email, String rawPassword, String name, Long createdBy) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        MemberJpaEntity entity = new MemberJpaEntity(
                null,
                email,
                passwordEncoder.encode(rawPassword),
                name,
                LocalDateTime.now()
        );

        MemberJpaEntity savedMember = memberRepository.save(entity);
        
        // ADMIN 역할 할당
        authorityService.assignRoleToMember(savedMember.getId(), "ADMIN", createdBy);

        return savedMember;
    }
}
