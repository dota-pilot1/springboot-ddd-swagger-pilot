package com.example.chatbot.auth.infrastructure.persistence.jpa;

import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.MemberRoleJpaEntity;
import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberRoleJpaRepository extends JpaRepository<MemberRoleJpaEntity, Long> {
    
    List<MemberRoleJpaEntity> findByMemberId(Long memberId);
    
    @Query("SELECT mr FROM MemberRoleJpaEntity mr JOIN FETCH mr.role r LEFT JOIN FETCH r.authorities WHERE mr.member.id = :memberId")
    List<MemberRoleJpaEntity> findByMemberIdWithRoleAndAuthorities(@Param("memberId") Long memberId);
    
    Optional<MemberRoleJpaEntity> findByMemberIdAndRoleId(Long memberId, Long roleId);
    
    boolean existsByMemberIdAndRoleId(Long memberId, Long roleId);
    
    void deleteByMemberIdAndRoleId(Long memberId, Long roleId);
    
    @Query("SELECT mr.role FROM MemberRoleJpaEntity mr WHERE mr.member.id = :memberId")
    Set<RoleJpaEntity> findRolesByMemberId(@Param("memberId") Long memberId);
}