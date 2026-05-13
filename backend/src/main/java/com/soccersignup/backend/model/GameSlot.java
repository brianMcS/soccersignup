package com.soccersignup.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "game_slots", uniqueConstraints = @UniqueConstraint(columnNames =  {"game_id", "player_id"}))
public class GameSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    private SlotStatus status = SlotStatus.CONFIRMED;

    @Column(nullable = false)
    private LocalDateTime signedUpAt;

    public GameSlot() {
    }

    public GameSlot(LocalDate gameDate, Player player) {
        this.player = player;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSignedUpAt() {
        return signedUpAt;
    }

    public void setSignedUpAt(LocalDateTime signedUpAt) {
        this.signedUpAt = signedUpAt;
    }
}