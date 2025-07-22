package com.hamidou.whatsappclone.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Configure la chaîne de filtres de sécurité de Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Active le support de CORS (Cross-Origin Resource Sharing)
                .cors(Customizer.withDefaults())

                // Désactive la protection CSRF (utile pour les APIs REST sans session)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure les autorisations d'accès aux endpoints
                .authorizeHttpRequests(req ->
                        req
                                // Autorise sans authentification certaines routes : authentification, Swagger, WebSocket, etc.
                                .requestMatchers(
                                        "/auth/**",
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html",
                                        "/ws/**"
                                ).permitAll()

                                // Toutes les autres requêtes nécessitent une authentification
                                .anyRequest().authenticated()
                )

                // Configure le serveur de ressources OAuth2 avec validation des tokens JWT (ex. Keycloak)
                .oauth2ResourceServer(auth ->
                        auth.jwt(token ->
                                // Utilise un convertisseur personnalisé pour extraire les rôles Keycloak
                                token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    // Configure un filtre CORS global pour autoriser le frontend Angular (http://localhost:4200)
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        // Autorise l’envoi de cookies (par ex. token JWT dans l'en-tête)
        config.setAllowCredentials(true);

        // Autorise uniquement le frontend Angular (adaptable en production)
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

        // Autorise certains en-têtes HTTP (notamment Authorization)
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION
        ));

        // Autorise certaines méthodes HTTP
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // Applique la config à toutes les routes
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

