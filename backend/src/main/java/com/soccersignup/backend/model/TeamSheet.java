package com.soccersignup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_sheets")
public class TeamSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, unique = true)
    private Game game;

    @OneToMany(mappedBy = "teamSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamSheetEntry> entries = new ArrayList<>();

    // False = draft (only organisers can see it)
    // True  = published (all players can see it)
    @Column(nullable = false)
    private boolean published = false;

    @Column
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public TeamSheet() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public List<TeamSheetEntry> getEntries() { return entries; }
    public void setEntries(List<TeamSheetEntry> entries) { this.entries = entries; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
