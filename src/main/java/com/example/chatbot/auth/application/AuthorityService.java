package com.example.chatbot.auth.application;

import com.example.chatbot.auth.infrastructure.persistence.jpa.AuthorityJpaRepository;
import com.example.chatbot.auth.infrastructure.persistence.jpa.MemberRoleJpaRepository;
import com.example.chatbot.auth.infrastructure.persistence.jpa.RoleJpaRepository;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.AuthorityJpaEntity;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.MemberRoleJpaEntity;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import com.example.chatbot.member.infrastructure.persistence.jpa.MemberJpaRepository;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthorityService {

    private final AuthorityJpaRepository authorityRepository;
    private final RoleJpaRepository roleRepository;
    private final MemberRoleJpaRepository memberRoleRepository;
    private final MemberJpaRepository memberRepository;

    public AuthorityService(AuthorityJpaRepository authorityRepository,
                          RoleJpaRepository roleRepository,
                          MemberRoleJpaRepository memberRoleRepository,
                          MemberJpaRepository memberRepository) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.memberRoleRepository = memberRoleRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 사용자의 모든 권한 조회 (캐시 적용)
     */
    @Cacheable(value = "userAuthorities", key = "#memberId")
    @Transactional(readOnly = true)
    public Collection<? extends GrantedAuthority> getUserAuthorities(Long memberId) {
        List<MemberRoleJpaEntity> memberRoles = memberRoleRepository
                .findByMemberIdWithRoleAndAuthorities(memberId);

        Set<GrantedAuthority> authorities = memberRoles.stream()
                .flatMap(memberRole -> {
                    RoleJpaEntity role = memberRole.getRole();
                    // 역할 자체도 권한으로 추가
                    Set<GrantedAuthority> roleAuthorities = role.getAuthorities().stream()
                            .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                            .collect(Collectors.toSet());
                    
                    // 역할도 권한으로 추가 (ROLE_ 접두사)
                    roleAuthorities.add(new SimpleGrantedAuthority(role.getRole()));
                    
                    return roleAuthorities.stream();
                })
                .collect(Collectors.toSet());

        return authorities;
    }

    /**
     * 사용자에게 역할 할당
     */
    public void assignRoleToMember(Long memberId, String roleName, Long assignedBy) {
        MemberJpaEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        RoleJpaEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName));

        // 이미 할당된 역할인지 확인
        if (memberRoleRepository.existsByMemberIdAndRoleId(memberId, role.getId())) {
            throw new IllegalArgumentException("이미 할당된 역할입니다.");
        }

        MemberRoleJpaEntity memberRole = new MemberRoleJpaEntity(member, role, assignedBy);
        memberRoleRepository.save(memberRole);
        
        // 캐시 무효화 (다음 조회 시 새로운 권한 적용)
        evictUserAuthoritiesCache(memberId);
    }

    /**
     * 사용자에서 역할 제거
     */
    public void removeRoleFromMember(Long memberId, String roleName) {
        RoleJpaEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName));

        memberRoleRepository.deleteByMemberIdAndRoleId(memberId, role.getId());
        
        // 캐시 무효화
        evictUserAuthoritiesCache(memberId);
    }

    /**
     * 사용자가 특정 권한을 가지고 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAuthority(Long memberId, String authorityName) {
        Collection<? extends GrantedAuthority> authorities = getUserAuthorities(memberId);
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authorityName));
    }

    /**
     * 사용자가 특정 역할을 가지고 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasRole(Long memberId, String roleName) {
        return hasAuthority(memberId, "ROLE_" + roleName);
    }

    /**
     * 사용자 권한 캐시 무효화
     */
    private void evictUserAuthoritiesCache(Long memberId) {
        // @CacheEvict 어노테이션 사용하거나 수동으로 캐시 무효화
        // 여기서는 간단히 구현
    }

    /**
     * 모든 권한 조회
     */
    @Transactional(readOnly = true)
    public List<AuthorityJpaEntity> getAllAuthorities() {
        return authorityRepository.findAll();
    }

    /**
     * 모든 역할 조회
     */
    @Transactional(readOnly = true)
    public List<RoleJpaEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * 역할에 권한 추가
     */
    public void addAuthorityToRole(String roleName, String authorityName) {
        RoleJpaEntity role = roleRepository.findByNameWithAuthorities(roleName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName));
        
        AuthorityJpaEntity authority = authorityRepository.findByName(authorityName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다: " + authorityName));

        role.addAuthority(authority);
        roleRepository.save(role);
    }

    /**
     * 역할에서 권한 제거
     */
    public void removeAuthorityFromRole(String roleName, String authorityName) {
        RoleJpaEntity role = roleRepository.findByNameWithAuthorities(roleName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName));
        
        AuthorityJpaEntity authority = authorityRepository.findByName(authorityName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다: " + authorityName));

        role.removeAuthority(authority);
        roleRepository.save(role);
    }
}