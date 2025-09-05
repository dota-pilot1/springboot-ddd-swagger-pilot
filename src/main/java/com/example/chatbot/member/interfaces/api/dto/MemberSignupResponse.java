package com.example.chatbot.member.interfaces.api.dto;

public class MemberSignupResponse {
    private Long id;
    private String email;
    private String name;

    public MemberSignupResponse() {}

    public MemberSignupResponse(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}

