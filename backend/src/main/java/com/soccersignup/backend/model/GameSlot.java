package com.soccersignup.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="game_slots")
public class GameSlot {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private LocalDate gameDate; // e.g. the Monday of that game week

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private Long userId; // or UUID/String if you're using that

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public GameSlot() {}

    public GameSlot(LocalDate gameDate, String playerName, Long userId, LocalDateTime timestamp) {
        this.gameDate = gameDate;
        this.playerName = playerName;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() { return id; }

    public LocalDate getGameDate() { return gameDate; }

    public void setGameDate(LocalDate gameDate) { this.gameDate = gameDate; }

    public String getPlayerName() { return playerName; }

    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}