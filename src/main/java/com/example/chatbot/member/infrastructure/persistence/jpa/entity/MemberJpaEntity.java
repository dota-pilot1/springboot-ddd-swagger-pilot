package com.example.chatbot.member.infrastructure.persistence.jpa.entity;

import com.example.chatbot.auth.infrastructure.persistence.jpa.entity.MemberRoleJpaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members", indexes = {@Index(columnList = "email", name = "idx_members_email")})
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberRoleJpaEntity> memberRoles = new HashSet<>();

    protected MemberJpaEntity() {}

    public MemberJpaEntity(Long id, String email, String password, String name, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    // 하위 호환성을 위한 생성자 (기존 role 파라미터 무시)
    @Deprecated
    public MemberJpaEntity(Long id, String email, String password, String name, String role, LocalDateTime createdAt) {
        this(id, email, password, name, createdAt);
    }

    // getters/setters for JPA
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Set<MemberRoleJpaEntity> getMemberRoles() { return memberRoles; }
    public void setMemberRoles(Set<MemberRoleJpaEntity> memberRoles) { this.memberRoles = memberRoles; }

    // 하위 호환성을 위한 메소드들
    @Deprecated
    public String getRole() { 
        // 첫 번째 역할 반환 (기존 코드 호환성)
        return memberRoles.isEmpty() ? "ROLE_USER" : "ROLE_" + memberRoles.iterator().next().getRole().getName();
    }

    @Deprecated
    public void setRole(String role) {
        // 기존 코드 호환성을 위해 빈 메소드로 유지
    }
}
