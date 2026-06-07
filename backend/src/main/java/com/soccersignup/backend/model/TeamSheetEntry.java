package com.soccersignup.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "team_sheet_entries")
public class TeamSheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_sheet_id", nullable = false)
    private TeamSheet teamSheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // Which side this player is on
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamSide teamSide;

    // Jersey/shirt number shown on the pitch graphic
    @Column(nullable = false)
    private int jerseyNumber;

    // Position on the pitch as a percentage of pitch width (0.0 - 100.0)
    // Stored as percentage so the layout scales to any screen size
    @Column(nullable = false)
    private double positionX;

    // Position on the pitch as a percentage of pitch height (0.0 - 100.0)
    @Column(nullable = false)
    private double positionY;

    public TeamSheetEntry() {}

    // Getters and setters
    public Long getId() { return id; }

    public TeamSheet getTeamSheet() { return teamSheet; }
    public void setTeamSheet(TeamSheet teamSheet) { this.teamSheet = teamSheet; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public TeamSide getTeamSide() { return teamSide; }
    public void setTeamSide(TeamSide teamSide) { this.teamSide = teamSide; }

    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public double getPositionX() { return positionX; }
    public void setPositionX(double positionX) { this.positionX = positionX; }

    public double getPositionY() { return positionY; }
    public void setPositionY(double positionY) { this.positionY = positionY; }

}
