package com.soccersignup.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.repository.PlayerRepository;
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
    private final PlayerRepository playerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(PlayerRepository playerRepository,
                                              JwtTokenProvider jwtTokenProvider,
                                              ObjectMapper objectMapper) {
        this.playerRepository = playerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
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
            Player player = playerRepository.findByEmail(email)
                    .orElseGet(() -> {
                        Player newPlayer = new Player(name, email, null, provider, oauthProviderId);
                        newPlayer.addRole(PlayerRole.PLAYER);
                        return playerRepository.save(newPlayer);
                    });

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(player);

            // Return JSON response instead of redirect
            sendSuccessResponse(response, token, player);

        } catch (Exception e) {
            sendErrorResponse(response, "Authentication failed: " + e.getMessage(), 500);
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String token, Player player) throws IOException {
        // Check if this is a popup request (detect via Accept header or a query param)
        String acceptHeader = response.getHeader("Accept");

        // Return HTML that postMessages to parent window
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>OAuth Callback</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Authentication successful. Closing...</p>\n" +
                "    <script>\n" +
                "        const message = {\n" +
                "            success: true,\n" +
                "            token: '" + token + "',\n" +
                "            player: {\n" +
                "                id: " + player.getId() + ",\n" +
                "                email: '" + player.getEmail() + "',\n" +
                "                name: '" + player.getName() + "',\n" +
                "                roles: " + objectMapper.writeValueAsString(
                player.getRoles().stream().map(Enum::toString).toList()) + "\n" +
                "            }\n" +
                "        };\n" +
                "        window.opener.postMessage(message, '*');\n" +
                "        window.close();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

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