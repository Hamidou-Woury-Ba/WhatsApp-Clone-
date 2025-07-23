package com.hamidou.whatsappclone.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
// Service responsable de la synchronisation des utilisateurs avec les données provenant de l'IdP
public class UserSynchronizer {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void synchronizeWithIdp(Jwt token) {
        // Affiche un log pour indiquer le début de la synchronisation
        log.info("Synchronizing user with idp");

        // Extrait l'email de l'utilisateur à partir du token JWT
        getUserEmail(token).ifPresent(userEmail -> {
            log.info("Synchronizing user having email {}", userEmail);

            // Vérifie si un utilisateur avec cet email existe déjà en base
            Optional<User> optUser = userRepository.findByEmail(userEmail);

            // Construit un objet User local à partir des informations du token
            User user = userMapper.fromTokenAttributes(token.getClaims());

            // Si l'utilisateur existe déjà, on garde son ID actuel
            optUser.ifPresent(value -> user.setId(value.getId()));

            // Enregistre (ou met à jour) l'utilisateur dans la base locale
            userRepository.save(user);
        });
    }

    // Méthode utilitaire pour extraire l'email du token JWT
    private Optional<String> getUserEmail(Jwt token) {
        Map<String, Object> attributes = token.getClaims();
        if (attributes.containsKey("email")) {
            return Optional.of(attributes.get("email").toString());
        }
        return Optional.empty();
    }
}

