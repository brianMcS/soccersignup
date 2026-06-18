package com.soccersignup.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccersignup.backend.dto.CreateGamesRequest;
import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.dto.RegistrationRequest;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void registrationIsPublicAndReturnsToken() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "Alex Morgan",
                "alex@example.com",
                "0871234567",
                "strong-password");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.player.email").value("alex@example.com"));
    }

    @Test
    void gamesAndNotificationsRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.fieldErrors").isMap());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void playerDirectoryIsRestrictedToAdministrators() throws Exception {
        String playerToken = tokenFor("player@example.com", PlayerRole.PLAYER);
        String organiserToken = tokenFor(
                "organiser@example.com",
                PlayerRole.PLAYER,
                PlayerRole.ORGANISER);
        String adminToken = tokenFor(
                "admin@example.com",
                PlayerRole.PLAYER,
                PlayerRole.ADMIN);

        mockMvc.perform(get("/api/players")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You do not have permission to perform this action"))
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/players")
                        .header("Authorization", "Bearer " + organiserToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/players")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void gameDetailUsesTheIdFromTheRequestPath() throws Exception {
        Game game = new Game();
        game.setGameDate(LocalDate.now().plusDays(1));
        game.setKickOffTime(LocalTime.of(19, 0));
        game.setLocation("Dublin");
        Game savedGame = gameRepository.saveAndFlush(game);
        String playerToken = tokenFor("player@example.com", PlayerRole.PLAYER);

        mockMvc.perform(get("/api/games/{id}", savedGame.getId())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedGame.getId()))
                .andExpect(jsonPath("$.location").value("Dublin"));
    }

    @Test
    void batchGameCreationCreatesAllGamesInOneRequest() throws Exception {
        String organiserToken = tokenFor(
                "organiser@example.com",
                PlayerRole.PLAYER,
                PlayerRole.ORGANISER);
        CreateGamesRequest request = new CreateGamesRequest(List.of(
                gameRequest(LocalDate.now().plusDays(1), 14),
                gameRequest(LocalDate.now().plusDays(8), 14)
        ));

        mockMvc.perform(post("/api/games/batch")
                        .header("Authorization", "Bearer " + organiserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].location").value("Dublin"))
                .andExpect(jsonPath("$[1].location").value("Dublin"));
    }

    @Test
    void batchGameCreationRejectsInvalidBatchBeforeCreatingAnyGames() throws Exception {
        String organiserToken = tokenFor(
                "organiser-invalid@example.com",
                PlayerRole.PLAYER,
                PlayerRole.ORGANISER);
        long gamesBefore = gameRepository.count();
        CreateGamesRequest request = new CreateGamesRequest(List.of(
                gameRequest(LocalDate.now().plusDays(1), 14),
                gameRequest(LocalDate.now().plusDays(8), 0)
        ));

        mockMvc.perform(post("/api/games/batch")
                        .header("Authorization", "Bearer " + organiserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").isMap());

        assertThat(gameRepository.count()).isEqualTo(gamesBefore);
    }

    private String tokenFor(String email, PlayerRole... roles) {
        Player player = new Player(email, email, null);
        for (PlayerRole role : roles) {
            player.addRole(role);
        }
        Player saved = playerRepository.saveAndFlush(player);
        return jwtTokenProvider.generateToken(saved);
    }

    private GameRequest gameRequest(LocalDate gameDate, int maxPlayers) {
        return new GameRequest(
                gameDate,
                LocalTime.of(19, 0),
                "Dublin",
                maxPlayers,
                null,
                null);
    }
}
