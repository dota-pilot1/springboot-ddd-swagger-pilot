package com.example.chatbot.auth.infrastructure.persistence.jpa;

import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.AuthorityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface AuthorityJpaRepository extends JpaRepository<AuthorityJpaEntity, Long> {
    
    Optional<AuthorityJpaEntity> findByName(String name);
    
    boolean existsByName(String name);
    
    Set<AuthorityJpaEntity> findByNameIn(Set<String> names);
}