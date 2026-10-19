package com.winwin.auth.process;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.winwin.auth.users.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessService {

    private final RestClient restClient;
    private final ProcessingLogRepository processingLogRepository;

    private record TransformRequest(String text) {}
    private record TransformResponse(String result) {}

    public ProcessService(
        ProcessingLogRepository processingLogRepository,
        @Value("${transform.service.url:http://localhost:8081}") String transformServiceUrl,
        @Value("${internal.token:my-secret-internal-token}") String internalToken
    ) {
        this.processingLogRepository = processingLogRepository;
        this.restClient = RestClient.builder()
            .baseUrl(transformServiceUrl)
            .defaultHeader("X-Internal-Token", internalToken)
            .build();
    }

    public ProcessDTO.Response process(ProcessDTO.Request request, User user) {
        String inputText = (request != null && request.text() != null) ? request.text() : "";

        try {
            log.info("Sending transform request for text: '{}'", inputText);
            TransformResponse transformResponse = restClient.post()
                .uri("/api/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TransformRequest(inputText))
                .retrieve()
                .body(TransformResponse.class);

            String result = transformResponse != null ? transformResponse.result() : "";

            ProcessingLog logEntity = ProcessingLog.builder()
                .user(user)
                .requestPayload(inputText)
                .responsePayload(result)
                .status("SUCCESS")
                .build();

            ProcessingLog saved = processingLogRepository.save(logEntity);
            log.info("Saved processing log with id: {}", saved.getId());

            return ProcessDTO.Response.builder()
                .logId(saved.getId())
                .originalText(inputText)
                .result(result)
                .status("SUCCESS")
                .createdAt(saved.getCreatedAt())
                .build();

        } catch (Exception e) {
            log.error("Error communicating with transform-service (data-api): {}", e.getMessage());

            ProcessingLog failedLog = ProcessingLog.builder()
                .user(user)
                .requestPayload(inputText)
                .responsePayload("Error: " + e.getMessage())
                .status("FAILED")
                .build();

            processingLogRepository.save(failedLog);

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call data-api: " + e.getMessage(), e);
        }
    }
}

