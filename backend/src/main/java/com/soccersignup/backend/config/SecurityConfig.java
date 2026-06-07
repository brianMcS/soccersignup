package com.soccersignup.backend.config;

import com.soccersignup.backend.security.JwtAuthenticationFilter;
import com.soccersignup.backend.security.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                           JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        // OAuth2 needs a session briefly for the state param handshake,
                        // but we won't use it for API auth after that
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/api/players/**",
                                "/api/games/**",
                                "/api/game-slots/**",
                                "/api/notifications/**",
                                "/api/dev/**",
                                "/oauth2/**",
                                "/login/**",
                                "/health"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth ->
                        oauth.successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .exceptionHandling(ex ->
                        // Return 401 JSON instead of redirecting to Google login
                        // when a JWT-protected endpoint is hit without a token
                        ex.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                                }
                        )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // old version
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(Customizer.withDefaults())
//                .csrf(csrf -> csrf.disable())
//                .httpBasic(httpBasic -> httpBasic.disable())
//                .formLogin(form -> form.disable())
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
//
//        return http.build();
//    }
}
