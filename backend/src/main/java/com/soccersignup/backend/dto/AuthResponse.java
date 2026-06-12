package com.soccersignup.backend.dto;

public record AuthResponse(
        boolean success,
        String token,
        PlayerResponse player
) {}
