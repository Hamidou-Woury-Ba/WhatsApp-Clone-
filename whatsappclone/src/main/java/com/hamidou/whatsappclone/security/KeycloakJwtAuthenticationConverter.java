package com.hamidou.whatsappclone.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Convertisseur personnalisé pour extraire les rôles Keycloak d’un token JWT
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // Convertit le JWT en token d’authentification utilisé par Spring Security
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        return new JwtAuthenticationToken(
                source,
                // Combine les rôles extraits par défaut et ceux personnalisés
                Stream.concat(
                        new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                        extractRessourceRoles(source).stream()
                ).collect(Collectors.toSet())
        );
    }

    // Extrait les rôles déclarés dans "resource_access" du token Keycloak
    private Collection<? extends GrantedAuthority> extractRessourceRoles(Jwt jwt) {
        // Accède à la section "resource_access" du JWT
        var resourceAccess = new HashMap<>(jwt.getClaim("resource_access"));

        // Récupère les rôles associés au client "account" (ou autre nom configuré dans Keycloak)
        var eternal = (Map<String, List<String>>) resourceAccess.get("account");
        var roles = eternal.get("roles");

        // Convertit les rôles Keycloak en authorities Spring (ex: ROLE_ADMIN)
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
                .collect(Collectors.toSet());
    }
}

