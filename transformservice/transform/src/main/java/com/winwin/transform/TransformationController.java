package com.winwin.transform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class TransformationController {
    public record TransformRequest(String text) {}
    public record TransformResponse(String result) {}
    @PostMapping("/api/transform")
    public TransformResponse transform(@RequestBody TransformRequest request) {
        if (request == null || request.text() == null) {
            return new TransformResponse("");
        }
        String transformed = new StringBuilder(request.text()).reverse().toString();
        return new TransformResponse(transformed);
    }
}

  