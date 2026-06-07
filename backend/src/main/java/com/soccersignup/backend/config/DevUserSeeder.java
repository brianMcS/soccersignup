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
                    "Sanju Baskaran",
                    "player1@test.local",
                    "0870000001",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Sylwester Swed",
                    "player2@test.local",
                    "0870000002",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Peter Parker",
                    "player3@test.local",
                    "0870000003",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "John Doe",
                    "player4@test.local",
                    "0870000004",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Jane Smith",
                    "player5@test.local",
                    "0870000005",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Bruce Wayne",
                    "player6@test.local",
                    "0870000006",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Clark Kent",
                    "player7@test.local",
                    "0870000007",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Diana Prince",
                    "player8@test.local",
                    "0870000008",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Tony Stark",
                    "player9@test.local",
                    "0870000009",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Steve Rogers",
                    "player10@test.local",
                    "0870000010",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Natasha Romanoff",
                    "player11@test.local",
                    "0870000011",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Clint Barton",
                    "player12@test.local",
                    "0870000012",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Bruce Banner",
                    "player13@test.local",
                    "0870000013",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Thor Odinson",
                    "player14@test.local",
                    "0870000014",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Scott Lang",
                    "player15@test.local",
                    "0870000015",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Sam Wilson",
                    "player16@test.local",
                    "0870000016",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Bucky Barnes",
                    "player17@test.local",
                    "0870000017",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Stephen Strange",
                    "player18@test.local",
                    "0870000018",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Wanda Maximoff",
                    "player19@test.local",
                    "0870000019",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Vision",
                    "player20@test.local",
                    "0870000020",
                    Set.of(PlayerRole.PLAYER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Test Organiser",
                    "organiser@test.local",
                    "0870000021",
                    Set.of(PlayerRole.PLAYER, PlayerRole.ORGANISER)
            );

            createPlayerIfMissing(
                    playerRepository,
                    "Test Admin",
                    "admin@test.local",
                    "0870000022",
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
