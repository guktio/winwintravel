package com.winwin.auth.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winwin.auth.users.User;
import com.winwin.auth.users.UserService;
import com.winwin.auth.users.UsersDTO;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AuthService {

    private final JwtService jwtService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthDTO.Response register(AuthDTO.Request request){
        if (userService.existsByEmail(request.email())) {
            throw new EntityExistsException("User with this mail already exists! Try to login.");
        }
        String rawPassword = request.password();
        User encodedUser = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(rawPassword))
                .build();
        userService.create(encodedUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.email(), rawPassword);

        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        User user = principal.getUser();

        String token = jwtService.generateJwt(user);
        var dto = UsersDTO.User.builder()
                .id(user.getId())
                .roles(user.getRoles())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().toString())
                .build();
        return AuthDTO.Response.builder()
                            .token(token)
                            .user(dto)
                            .build();
    }    
    
    public AuthDTO.Response login(AuthDTO.Request request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
            
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        User user = principal.getUser();
        var dto = UsersDTO.User.builder()
                .id(user.getId())
                .roles(user.getRoles())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().toString())
                .build();
        String token = jwtService.generateJwt(user);
        return AuthDTO.Response.builder()
                            .token(token)
                            .user(dto)
                            .build();
    }
    
}
