package com.soccersignup.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate gameDate;

    private LocalTime kickOffTime;
    private String location;
    private int maxPlayers = 18;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.OPEN;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSlot> slots = new ArrayList<>();

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private TeamSheet teamSheet;

    public Game() {
    }

    public Game(LocalDate gameDate, LocalDateTime createdAt, List<GameSlot> slots) {
        this.gameDate = gameDate;
        this.createdAt = createdAt;
        this.slots = slots;
    }

    public SlotStatus determineSignupStatus(long confirmedCount) {
        return confirmedCount >= maxPlayers
                ? SlotStatus.WAITLISTED
                : SlotStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
    }

    public LocalTime getKickOffTime() {
        return kickOffTime;
    }

    public void setKickOffTime(LocalTime kickOffTime) {
        this.kickOffTime = kickOffTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<GameSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<GameSlot> slots) {
        this.slots = slots;
    }

    public TeamSheet getTeamSheet() {
        return teamSheet;
    }

    public void setTeamSheet(TeamSheet teamSheet) {
        this.teamSheet = teamSheet;
    }
}
