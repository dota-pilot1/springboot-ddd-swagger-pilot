package com.example.chatbot.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long memberId;
    private String email;
    private String name;

    public LoginResponse(String accessToken, Long memberId, String email, String name) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.memberId = memberId;
        this.email = email;
        this.name = name;
    }
}