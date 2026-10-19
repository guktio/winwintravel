package com.winwin.auth.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.auth.users.User;
import com.winwin.auth.users.UsersDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody AuthDTO.Request req){
        log.info("GET /register {}", req.email());
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.Response> login(@RequestBody AuthDTO.Request req){
        log.info("GET /login {}", req.email());
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UsersDTO.User> me(@CurrentUser User user){
        log.info("GET /me");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var dto = UsersDTO.User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().toString())
                .roles(user.getRoles())
                .build();
        return ResponseEntity.ok(dto);
    }
    
}

