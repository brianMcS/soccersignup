package com.soccersignup.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.PlayerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final PlayerService playerService;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                              ObjectMapper objectMapper,
                                              PlayerService playerService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.playerService = playerService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Determine provider from authentication
        OAuthProvider provider;
        if (authentication instanceof OAuth2AuthenticationToken) {
            String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            if ("github".equalsIgnoreCase(registrationId)) {
                provider = OAuthProvider.GITHUB;
            } else if ("google".equalsIgnoreCase(registrationId)) {
                provider = OAuthProvider.GOOGLE;
            } else {
                provider = OAuthProvider.GOOGLE;
            }
        } else {
            provider = OAuthProvider.GOOGLE;
        }

        // Extract attributes with fallbacks for different providers
        String email = Optional.ofNullable(oAuth2User.getAttribute("email"))
                .orElse(Optional.ofNullable(oAuth2User.getAttribute("email_address")).orElse(null)).toString();
        String name = Optional.ofNullable(oAuth2User.getAttribute("name"))
                .orElse(Optional.ofNullable(oAuth2User.getAttribute("login")).orElse("Unknown")).toString();
        String oauthProviderId = oAuth2User.getName();

        // Validate email
        if (email == null || email.isBlank()) {
            sendErrorResponse(response, "Email not provided by OAuth provider", 400);
            return;
        }

        try {
            // Find or create player
            Player player = playerService.findOrCreateOAuthPlayer(email, name, provider, oauthProviderId);
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(player);

            // Return JSON response instead of redirect
            sendSuccessResponse(response, token, player);

        } catch (Exception e) {
            sendErrorResponse(response, "Authentication failed: " + e.getMessage(), 500);
        }
    }

    private void sendSuccessResponse(HttpServletResponse response,
                                     String token, Player player) throws IOException {
        // Build a data object and serialize it safely
        Map<String, Object> playerData = Map.of(
                "id", player.getId(),
                "email", player.getEmail(),
                "name", player.getName(),
                "roles", player.getRoles().stream().map(Enum::toString).toList()
        );
        Map<String, Object> message = Map.of(
                "success", true,
                "token", token,
                "player", playerData
        );
        // Serialize the whole message safely and embed as a single JSON string
        String safeJson = objectMapper.writeValueAsString(message);

        String html = """
        <!DOCTYPE html>
        <html><head><title>OAuth Callback</title></head>
        <body>
            <p>Authentication successful. Closing...</p>
            <script>
                window.opener.postMessage(%s, '*');
                window.close();
            </script>
        </body></html>
        """.formatted(safeJson);  // safeJson is Jackson-escaped, safe to embed

        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(html);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("error", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorMap));
    }
}