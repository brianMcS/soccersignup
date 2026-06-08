package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.dto.NotificationResponse;
import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.Notification;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.repository.NotificationRepository;
import com.soccersignup.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForPlayer(Player player) {
        return notificationRepository
                .findByPlayerOrderByCreatedAtDesc(player)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Player player) {
        return notificationRepository.countByPlayerAndReadFalse(player);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Player player) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        // Make sure a player can't mark someone else's notification as read
        if (!notification.getPlayer().getId().equals(player.getId())) {
            throw new IllegalStateException("Not your notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Player player) {
        List<Notification> unread = notificationRepository
                .findByPlayerOrderByCreatedAtDesc(player)
                .stream()
                .filter(n -> !n.isRead())
                .toList();

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}