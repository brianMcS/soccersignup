package com.soccersignup.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, Map.of());
    }
}
