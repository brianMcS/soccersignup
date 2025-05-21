package com.soccersignup.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    @Column(name = "is_attending")
    private Boolean isAttending;


    public Player() {}

    public Player(String name, String email, String phone, Boolean isAttending) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAttending = isAttending;
    }
    

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIsAttending() { return isAttending; }

    public void setIsAttending(Boolean isAttending) { this.isAttending = isAttending; }
}
