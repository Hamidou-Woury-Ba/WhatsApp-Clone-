package com.hamidou.whatsappclone.interceptor;

import com.hamidou.whatsappclone.user.UserSynchronizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Rend ce filtre accessible à Spring (sera injecté automatiquement)
@RequiredArgsConstructor // Génère un constructeur avec les attributs "final"
public class UserSynchronizerFilter extends OncePerRequestFilter {

    // Service chargé de synchroniser l'utilisateur avec les données venant du token JWT
    private final UserSynchronizer userSynchronizer;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Vérifie que l'utilisateur est bien authentifié (pas anonyme)
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            // Récupère le token JWT de l'utilisateur connecté
            JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            // Lance la synchronisation avec l'IdP (par exemple Keycloak)
            userSynchronizer.synchronizeWithIdp(token.getToken());
        }

        // Poursuit le traitement de la requête HTTP
        filterChain.doFilter(request, response);
    }
}

