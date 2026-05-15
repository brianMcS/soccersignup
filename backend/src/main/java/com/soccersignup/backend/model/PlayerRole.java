package com.soccersignup.backend.model;

public enum PlayerRole {
    PLAYER("Player - Can join games"),
    ORGANIZER("Organizer - Can create and manage games"),
    ADMIN("Admin - Full system access");

    private final String description;

    PlayerRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}