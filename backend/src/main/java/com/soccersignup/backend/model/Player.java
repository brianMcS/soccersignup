package com.soccersignup.backend.model;

import jakarta.persistence.Entity;

@Entity
public class Player {
    private Long id;
    private String name;
    private String position;

}
