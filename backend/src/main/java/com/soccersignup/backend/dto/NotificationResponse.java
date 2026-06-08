package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String message,
        String link,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from (Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
