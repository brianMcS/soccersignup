package com.soccersignup.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PlayerService playerService;
    @Mock
    private Authentication authentication;
    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(
                jwtTokenProvider,
                new ObjectMapper(),
                playerService,
                "http://localhost:4200");
    }

    @Test
    void sendsTokenOnlyToConfiguredFrontendOrigin() throws Exception {
        Player player = new Player();
        player.setId(1L);
        player.setName("Alex");
        player.setEmail("alex@example.com");
        player.setRoles(Set.of(PlayerRole.PLAYER));

        OAuth2User oAuth2User = oauthUser(Map.of(
                "sub", "google-user-id",
                "email", "alex@example.com",
                "name", "Alex"));
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(playerService.findOrCreateOAuthPlayer(
                "alex@example.com", "Alex", OAuthProvider.GOOGLE, "google-user-id"))
                .thenReturn(player);
        when(jwtTokenProvider.generateToken(player)).thenReturn("signed-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
                .contains("postMessage(")
                .contains("\"http://localhost:4200\"")
                .doesNotContain("postMessage(%s, '*')")
                .doesNotContain("\"*\"");
    }

    @Test
    void returnsBadRequestWhenProviderDoesNotSupplyEmail() throws Exception {
        OAuth2User oAuth2User = oauthUser(Map.of(
                "sub", "google-user-id",
                "name", "Alex"));
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .contains("Email not provided by OAuth provider");
        verify(playerService, never()).findOrCreateOAuthPlayer(
                any(), any(), any(), any());
    }

    private OAuth2User oauthUser(Map<String, Object> attributes) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub");
    }
}
