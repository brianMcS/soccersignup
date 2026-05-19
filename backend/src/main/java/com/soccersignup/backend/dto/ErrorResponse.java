package com.soccersignup.backend.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String error,
        int status,
        LocalDateTime timestamp
) {}
