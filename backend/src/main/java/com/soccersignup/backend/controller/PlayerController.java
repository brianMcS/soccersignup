package com.soccersignup.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.PlayerService;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService){
        this.playerService= playerService;
    }

    @GetMapping
    public List<Player> getAllPlayers(){
        return playerService.getAllPlayers();
    }
    
    
}
