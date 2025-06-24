package com.soccersignup.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate gameDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSlot> slots = new ArrayList<>();

    public Game() {
    }

    public Game(LocalDate gameDate, LocalDateTime createdAt, List<GameSlot> slots) {
        this.gameDate = gameDate;
        this.createdAt = createdAt;
        this.slots = slots;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public List<GameSlot> getGameSlots() {
        return slots;
    }
}
