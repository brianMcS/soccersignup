package com.soccersignup.backend.model;

public enum OAuthProvider {
    GOOGLE("Google"),
    GITHUB("GitHub");

    private final String displayName;

    OAuthProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
