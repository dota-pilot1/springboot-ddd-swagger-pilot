package com.example.chatbot.auth.infrastructure.persistence.jpa.entity;

import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_roles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleJpaEntity role;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by")
    private Long assignedBy; // 권한을 할당한 관리자 ID

    public MemberRoleJpaEntity(MemberJpaEntity member, RoleJpaEntity role, Long assignedBy) {
        this.member = member;
        this.role = role;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
    }
}