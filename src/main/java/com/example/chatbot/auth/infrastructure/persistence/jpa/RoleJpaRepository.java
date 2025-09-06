package com.example.chatbot.auth.infrastructure.persistence.jpa;

import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    
    Optional<RoleJpaEntity> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT r FROM RoleJpaEntity r LEFT JOIN FETCH r.authorities WHERE r.name = :name")
    Optional<RoleJpaEntity> findByNameWithAuthorities(@Param("name") String name);
    
    @Query("SELECT r FROM RoleJpaEntity r LEFT JOIN FETCH r.authorities WHERE r.id IN :roleIds")
    Set<RoleJpaEntity> findByIdInWithAuthorities(@Param("roleIds") Set<Long> roleIds);
}