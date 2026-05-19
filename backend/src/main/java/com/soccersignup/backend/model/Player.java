package com.soccersignup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, unique = false)
    private String phone;

    // OAuth2 Fields
    @Column(name = "oauth_provider", nullable = true)
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = true, unique = true)
    private String oauthProviderId;

    // Role-Based Access
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_roles", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<PlayerRole> roles = new HashSet<>();

    // Account Management
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Player() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Player(String name, String email, String phone,
                  OAuthProvider oauthProvider, String oauthProviderId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.isActive = true;
        this.roles = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Player(String name, String email, String phone){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
        this.roles = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public OAuthProvider getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(OAuthProvider oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthProviderId() { return oauthProviderId; }
    public void setOauthProviderId(String oauthProviderId) {
        this.oauthProviderId = oauthProviderId;
    }

    public Set<PlayerRole> getRoles() { return roles; }
    public void setRoles(Set<PlayerRole> roles) { this.roles = roles; }
    public void addRole(PlayerRole role) { this.roles.add(role); }
    public void removeRole(PlayerRole role) { this.roles.remove(role); }
    public boolean hasRole(PlayerRole role) { return this.roles.contains(role); }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
