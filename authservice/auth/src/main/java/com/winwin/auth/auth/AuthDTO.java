package com.winwin.auth.auth;

import com.winwin.auth.users.UsersDTO;

import lombok.Builder;

public class AuthDTO {

    private AuthDTO() {}

    @Builder 
    public record Response(
        UsersDTO.User user,
        String token
    ) {}

    @Builder 
    public record Request(
        String email,
        String password
    ) {}
    
}
