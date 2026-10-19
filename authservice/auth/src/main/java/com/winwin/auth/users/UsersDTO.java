package com.winwin.auth.users;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;

public class UsersDTO {

    private UsersDTO() {}

    @Builder 
    public record User(    
        UUID id,
        Set<Roles> roles,    
        String email,
        String createdAt
    ) {}
}
