package com.winwin.auth.process;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Builder;

public class ProcessDTO {

    private ProcessDTO() {}

    public record Request(
        @JsonAlias({"data", "input", "inputText", "requestPayload"})
        String text
    ) {}

    @Builder
    public record Response(
        UUID logId,
        String originalText,
        String result,
        String status,
        LocalDateTime createdAt
    ) {}
}

