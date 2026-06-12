package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.AuthResponse;
import com.soccersignup.backend.dto.LoginRequest;
import com.soccersignup.backend.dto.RegistrationRequest;
import com.soccersignup.backend.exception.InvalidCredentialsException;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(
                playerRepository,
                passwordEncoder,
                jwtTokenProvider);
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndReturnsToken() {
        RegistrationRequest request = new RegistrationRequest(
                " Alex Morgan ",
                " ALEX@Example.COM ",
                " 0871234567 ",
                "strong-password");

        when(playerRepository.findByEmailIgnoreCase("alex@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("strong-password")).thenReturn("hashed-password");
        when(playerRepository.save(org.mockito.ArgumentMatchers.any(Player.class)))
                .thenAnswer(invocation -> {
                    Player player = invocation.getArgument(0);
                    player.setId(1L);
                    return player;
                });
        when(jwtTokenProvider.generateToken(org.mockito.ArgumentMatchers.any(Player.class)))
                .thenReturn("signed-token");

        AuthResponse response = service.register(request);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player savedPlayer = playerCaptor.getValue();

        assertThat(savedPlayer.getName()).isEqualTo("Alex Morgan");
        assertThat(savedPlayer.getEmail()).isEqualTo("alex@example.com");
        assertThat(savedPlayer.getPhone()).isEqualTo("0871234567");
        assertThat(savedPlayer.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedPlayer.getRoles()).containsExactly(PlayerRole.PLAYER);
        assertThat(response.token()).isEqualTo("signed-token");
    }

    @Test
    void registrationRejectsExistingEmailIgnoringCase() {
        when(playerRepository.findByEmailIgnoreCase("alex@example.com"))
                .thenReturn(Optional.of(new Player()));

        assertThatThrownBy(() -> service.register(new RegistrationRequest(
                "Alex",
                "Alex@Example.com",
                null,
                "strong-password")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("An account already exists for this email");
    }

    @Test
    void loginReturnsTokenForActivePasswordAccount() {
        Player player = passwordPlayer();
        when(playerRepository.findByEmailIgnoreCase("alex@example.com"))
                .thenReturn(Optional.of(player));
        when(passwordEncoder.matches("strong-password", "hashed-password"))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(player)).thenReturn("signed-token");

        AuthResponse response = service.login(
                new LoginRequest(" ALEX@example.com ", "strong-password"));

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.player().email()).isEqualTo("alex@example.com");
    }

    @Test
    void loginUsesSameErrorForWrongPasswordAndOAuthOnlyAccount() {
        Player passwordPlayer = passwordPlayer();
        when(playerRepository.findByEmailIgnoreCase("alex@example.com"))
                .thenReturn(Optional.of(passwordPlayer));
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        assertInvalidLogin(new LoginRequest("alex@example.com", "wrong-password"));

        Player oauthOnlyPlayer = passwordPlayer();
        oauthOnlyPlayer.setPasswordHash(null);
        when(playerRepository.findByEmailIgnoreCase("oauth@example.com"))
                .thenReturn(Optional.of(oauthOnlyPlayer));

        assertInvalidLogin(new LoginRequest("oauth@example.com", "strong-password"));
    }

    private void assertInvalidLogin(LoginRequest request) {
        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    private Player passwordPlayer() {
        Player player = new Player("Alex", "alex@example.com", null);
        player.setId(1L);
        player.setPasswordHash("hashed-password");
        player.addRole(PlayerRole.PLAYER);
        return player;
    }
}
