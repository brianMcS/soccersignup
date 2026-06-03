package com.soccersignup.backend.config;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.repository.PlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile({"dev", "local"})
public class DevUserSeeder {

    @Bean
    CommandLineRunner seedDevUsers(PlayerRepository playerRepository) {
        return args -> {
            createPlayerIfMissing(
                    playerRepository,
                    "Test Player One",
                    "player1@test.local",
                    "0870000001",
                    Set.of(PlayerRole.PLAYER)
            );
            createPlayerIfMissing(
                    playerRepository,
                    "Test Player Two",
                    "player2@test.local",
                    "0870000002",
                    Set.of(PlayerRole.PLAYER)
            );
            createPlayerIfMissing(
                    playerRepository,
                    "Test Organiser",
                    "organiser@test.local",
                    "0870000003",
                    Set.of(PlayerRole.PLAYER, PlayerRole.ORGANISER)
            );
            createPlayerIfMissing(
                    playerRepository,
                    "Test Admin",
                    "admin@test.local",
                    "0870000004",
                    Set.of(PlayerRole.PLAYER, PlayerRole.ORGANISER, PlayerRole.ADMIN)
            );
        };
    }

    private void createPlayerIfMissing(
            PlayerRepository playerRepository,
            String name,
            String email,
            String phone,
            Set<PlayerRole> roles
    ) {
        playerRepository.findByEmail(email).orElseGet(() -> {
            Player player = new Player(name, email, phone);
            player.setRoles(roles);
            return playerRepository.save(player);
        });
    }
}
