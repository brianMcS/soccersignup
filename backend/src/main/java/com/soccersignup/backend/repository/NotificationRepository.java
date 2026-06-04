package com.soccersignup.backend.repository;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.Notification;
import com.soccersignup.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // All notifications for a player, newest first
    List<Notification> findByPlayerOrderByCreatedAtDesc(Player player);

    // Count unread — used for the bell badge number
    long countByPlayerAndReadFalse(Player player);

    // Used before re-publishing to clean up old unread notifications for this game
    // so players don't get spammed with duplicates
    void deleteByPlayerAndGameAndReadFalse(Player player, Game game);

    // Delete all unread notifications for a game across all players (bulk cleanup on re-publish)
    void deleteByGameAndReadFalse(Game game);
}
