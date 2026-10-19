package com.winwin.auth.process;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.auth.auth.CurrentUser;
import com.winwin.auth.users.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final ProcessService processService;

    @PostMapping
    public ResponseEntity<ProcessDTO.Response> process(
        @RequestBody ProcessDTO.Request request,
        @CurrentUser User user
    ) {
        log.info("POST /api/process received from user: {}", user != null ? user.getEmail() : "anonymous");
        ProcessDTO.Response response = processService.process(request, user);
        return ResponseEntity.ok(response);
    }
}

