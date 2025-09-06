package com.example.chatbot.auth.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String displayName;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_authorities",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<AuthorityJpaEntity> authorities = new HashSet<>();

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<MemberRoleJpaEntity> memberRoles = new HashSet<>();

    public RoleJpaEntity(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 역할에 권한 추가 (LazyInitializationException 방지)
     */
    public void addAuthority(AuthorityJpaEntity authority) {
        this.authorities.add(authority);
        // LazyInitializationException 방지를 위해 양방향 매핑 제거
        // authority.getRoles().add(this);
    }

    /**
     * 역할에서 권한 제거 (LazyInitializationException 방지)
     */
    public void removeAuthority(AuthorityJpaEntity authority) {
        this.authorities.remove(authority);
        // LazyInitializationException 방지를 위해 양방향 매핑 제거
        // authority.getRoles().remove(this);
    }

    /**
     * Spring Security에서 사용할 역할 문자열 반환
     */
    public String getRole() {
        return "ROLE_" + this.name;
    }
}