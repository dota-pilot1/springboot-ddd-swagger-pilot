package com.example.chatbot.auth.infrastructure;

import com.example.chatbot.auth.application.AuthorityService;
import com.example.chatbot.member.infrastructure.persistence.jpa.MemberJpaRepository;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberJpaRepository memberRepository;
    private final AuthorityService authorityService;

    public CustomUserDetailsService(MemberJpaRepository memberRepository, AuthorityService authorityService) {
        this.memberRepository = memberRepository;
        this.authorityService = authorityService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberJpaEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Collection<? extends GrantedAuthority> authorities = authorityService.getUserAuthorities(member.getId());

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}