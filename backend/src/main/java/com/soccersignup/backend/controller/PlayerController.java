package com.soccersignup.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.PlayerService;

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
    
    @GetMapping("path")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id)
            	.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player savedPlayer = playerService.savePlayer(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlayer);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player playerDetails) {
        return playerService.getPlayerById(id)
                .map(existingPlayer -> {
                    existingPlayer.setName(playerDetails.getName());
                    existingPlayer.setEmail(playerDetails.getEmail());
                    existingPlayer.setPhone(playerDetails.getPhone());
                    existingPlayer.setIsAttending(playerDetails.getIsAttending());
                    Player updatedPlayer = playerService.savePlayer(existingPlayer);
                    return ResponseEntity.ok(updatedPlayer);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
}
