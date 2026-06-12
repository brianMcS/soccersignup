package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.AuthResponse;
import com.soccersignup.backend.dto.LoginRequest;
import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.dto.RegistrationRequest;
import com.soccersignup.backend.exception.InvalidCredentialsException;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationService(
            PlayerRepository playerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (playerRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalStateException("An account already exists for this email");
        }

        Player player = new Player(
                request.name().trim(),
                email,
                normalizePhone(request.phone()));
        player.setPasswordHash(passwordEncoder.encode(request.password()));
        player.addRole(PlayerRole.PLAYER);

        return authenticatedResponse(playerRepository.save(player));
    }

    public AuthResponse login(LoginRequest request) {
        Player player = playerRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(candidate -> candidate.getIsActive()
                        && candidate.getPasswordHash() != null
                        && passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS));

        return authenticatedResponse(player);
    }

    private AuthResponse authenticatedResponse(Player player) {
        return new AuthResponse(
                true,
                jwtTokenProvider.generateToken(player),
                PlayerResponse.from(player));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
