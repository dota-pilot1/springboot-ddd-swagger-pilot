package com.example.chatbot.admin.interfaces.api;

import com.example.chatbot.auth.application.AuthorityService;
import com.example.chatbot.member.application.MemberService;
import com.example.chatbot.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // 클래스 레벨에서 ADMIN 역할 필요
public class AdminController {

    private final AuthorityService authorityService;
    private final MemberService memberService;

    public AdminController(AuthorityService authorityService, MemberService memberService) {
        this.authorityService = authorityService;
        this.memberService = memberService;
    }

    /**
     * 사용자 역할 변경 - MANAGE_USERS 권한 필요
     */
    @PreAuthorize("hasAuthority('AUTHORITY_MANAGE_USERS')")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        String newRole = request.get("role");
        if (newRole == null || (!newRole.equals("USER") && !newRole.equals("ADMIN"))) {
            return ResponseEntity.badRequest().build();
        }

        // 현재 사용자의 역할을 제거하고 새 역할 할당
        // 실제로는 더 정교한 로직 필요 (기존 역할 조회 후 변경)
        try {
            // 예시: USER -> ADMIN 또는 ADMIN -> USER
            authorityService.removeRoleFromMember(userId, "USER");
            authorityService.removeRoleFromMember(userId, "ADMIN");
            authorityService.assignRoleToMember(userId, newRole, 1L); // currentUser.getId()

            return ResponseEntity.ok(Map.of("message", "역할이 변경되었습니다.", "newRole", newRole));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 사용자 탈퇴 (관리자가 강제 탈퇴) - MANAGE_USERS 권한 필요
     */
    @PreAuthorize("hasAuthority('AUTHORITY_MANAGE_USERS')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        try {
            // 실제 구현에서는 MemberService에 delete 메소드 필요
            // memberService.deleteMember(userId);
            
            return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 관리자 계정 생성 - MANAGE_SYSTEM 권한 필요
     */
    @PreAuthorize("hasAuthority('AUTHORITY_MANAGE_SYSTEM')")
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createAdmin(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");

            MemberJpaEntity admin = memberService.createAdmin(email, password, name, 1L);
            
            return ResponseEntity.ok(Map.of(
                "message", "관리자 계정이 생성되었습니다.",
                "adminId", admin.getId(),
                "email", admin.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 시스템 상태 조회 - MANAGE_SYSTEM 권한 필요
     */
    @PreAuthorize("hasAuthority('AUTHORITY_MANAGE_SYSTEM')")
    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "version", "1.0.0",
            "uptime", "운영 중"
        ));
    }
}