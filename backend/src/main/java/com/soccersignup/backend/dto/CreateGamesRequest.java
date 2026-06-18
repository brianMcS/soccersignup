package com.soccersignup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGamesRequest(
        @NotEmpty(message = "At least one game is required")
        @Size(max = 52, message = "Cannot create more than 52 games at once")
        List<@Valid GameRequest> games
) {}
