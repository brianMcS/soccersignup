package com.soccersignup.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccersignup.backend.dto.RegistrationRequest;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
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
                .andExpect(status().isUnauthorized());

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
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/players")
                        .header("Authorization", "Bearer " + organiserToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/players")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String tokenFor(String email, PlayerRole... roles) {
        Player player = new Player(email, email, null);
        for (PlayerRole role : roles) {
            player.addRole(role);
        }
        Player saved = playerRepository.saveAndFlush(player);
        return jwtTokenProvider.generateToken(saved);
    }
}
