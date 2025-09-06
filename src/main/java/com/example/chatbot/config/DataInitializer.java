package com.example.chatbot.config;

import com.example.chatbot.auth.infrastructure.persistence.jpa.AuthorityJpaRepository;
import com.example.chatbot.auth.infrastructure.persistence.jpa.RoleJpaRepository;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.AuthorityJpaEntity;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AuthorityJpaRepository authorityRepository;
    private final RoleJpaRepository roleRepository;

    public DataInitializer(AuthorityJpaRepository authorityRepository, RoleJpaRepository roleRepository) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("데이터 초기화 시작...");
        
        createAuthoritiesIfNotExists();
        createRolesIfNotExists();
        assignAuthoritiesToRoles();
        
        log.info("데이터 초기화 완료!");
    }

    private void createAuthoritiesIfNotExists() {
        log.info("권한 데이터 초기화...");

        createAuthorityIfNotExists("MANAGE_USERS", "사용자 관리", "사용자를 관리할 수 있습니다 (탈퇴, 역할 변경 등)");
        createAuthorityIfNotExists("MANAGE_SYSTEM", "시스템 관리", "시스템 설정을 관리할 수 있습니다");
        
        log.info("권한 데이터 초기화 완료");
    }

    private void createRolesIfNotExists() {
        log.info("역할 데이터 초기화...");

        createRoleIfNotExists("USER", "일반 사용자", "기본 사용자 역할");
        createRoleIfNotExists("ADMIN", "관리자", "시스템 관리자 역할");
        
        log.info("역할 데이터 초기화 완료");
    }

    private void assignAuthoritiesToRoles() {
        log.info("역할별 권한 할당...");

        // USER 역할: 기본 권한 없음 (API 접근만)
        // ADMIN 역할: 모든 권한
        RoleJpaEntity adminRole = roleRepository.findByNameWithAuthorities("ADMIN").orElse(null);
        if (adminRole != null) {
            AuthorityJpaEntity manageUsers = authorityRepository.findByName("MANAGE_USERS").orElse(null);
            AuthorityJpaEntity manageSystem = authorityRepository.findByName("MANAGE_SYSTEM").orElse(null);

            if (manageUsers != null && !adminRole.getAuthorities().contains(manageUsers)) {
                adminRole.addAuthority(manageUsers);
                log.info("ADMIN 역할에 MANAGE_USERS 권한 추가");
            }

            if (manageSystem != null && !adminRole.getAuthorities().contains(manageSystem)) {
                adminRole.addAuthority(manageSystem);
                log.info("ADMIN 역할에 MANAGE_SYSTEM 권한 추가");
            }

            roleRepository.save(adminRole);
        }

        log.info("역할별 권한 할당 완료");
    }

    private void createAuthorityIfNotExists(String name, String displayName, String description) {
        if (!authorityRepository.existsByName(name)) {
            AuthorityJpaEntity authority = new AuthorityJpaEntity(name, displayName, description);
            authorityRepository.save(authority);
            log.info("권한 생성: {}", name);
        } else {
            log.debug("권한 이미 존재: {}", name);
        }
    }

    private void createRoleIfNotExists(String name, String displayName, String description) {
        if (!roleRepository.existsByName(name)) {
            RoleJpaEntity role = new RoleJpaEntity(name, displayName, description);
            roleRepository.save(role);
            log.info("역할 생성: {}", name);
        } else {
            log.debug("역할 이미 존재: {}", name);
        }
    }
}