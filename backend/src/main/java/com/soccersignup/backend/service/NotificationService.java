package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.NotificationResponse;
import com.soccersignup.backend.model.Player;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getNotificationsForPlayer(Player player);
    long getUnreadCount(Player player);
    void markAsRead(Long notificationId, Player player);
    void markAllAsRead(Player player);
}
