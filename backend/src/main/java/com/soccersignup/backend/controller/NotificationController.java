package com.soccersignup.backend.controller;

import com.soccersignup.backend.dto.NotificationResponse;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /api/notifications
    // Returns all notifications for the logged-in player
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        return ResponseEntity.ok(
                notificationService.getNotificationsForPlayer(player));
    }

    // GET /api/notifications/unread-count
    // Used by the nav bell to show the badge number
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(player);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // PATCH /api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        notificationService.markAsRead(id, player);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        notificationService.markAllAsRead(player);
        return ResponseEntity.noContent().build();
    }
}