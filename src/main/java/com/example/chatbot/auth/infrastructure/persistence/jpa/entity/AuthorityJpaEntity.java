package com.example.chatbot.auth.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authorities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityJpaEntity {

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

    @ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY)
    private Set<RoleJpaEntity> roles = new HashSet<>();

    public AuthorityJpaEntity(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Spring Security에서 사용할 권한 문자열 반환
     */
    public String getAuthority() {
        return "AUTHORITY_" + this.name;
    }
}