package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.Player;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record PlayerResponse(
        Long id,
        String name,
        String email,
        String phone,
        Set<String> roles,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getEmail(),
                player.getPhone(),
                player.getRoles().stream()
                        .map(Enum::toString)
                        .collect(Collectors.toSet()),
                player.getIsActive(),
                player.getCreatedAt()
        );
    }
}
