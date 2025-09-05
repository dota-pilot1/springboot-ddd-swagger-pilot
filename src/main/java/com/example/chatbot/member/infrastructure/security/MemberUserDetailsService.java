package com.example.chatbot.member.infrastructure.security;

import com.example.chatbot.member.infrastructure.persistence.jpa.MemberJpaRepository;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberJpaRepository memberRepository;

    public MemberUserDetailsService(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberJpaEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority(member.getRole());

        return org.springframework.security.core.userdetails.User.withUsername(member.getEmail())
                .password(member.getPassword())
                .authorities(List.of(authority))
                .build();
    }
}
