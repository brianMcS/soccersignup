package com.soccersignup.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record GameRequest(
        @NotNull LocalDate gameDate,
        @NotNull
        LocalTime kickOffTime,
        @NotNull String location,
        @Min(1) @Max(30) int maxPlayers,
        @DecimalMin("0.00") BigDecimal feeAmount,
        String revolutLink
) {}
