package com.hamidou.whatsappclone.user;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
// Classe utilitaire pour convertir les attributs d’un token JWT en objet User
public class UserMapper {

    public User fromTokenAttributes(Map<String, Object> attributes) {
        User user = new User();

        // Attribut "sub" : identifiant unique de l'utilisateur dans l'IdP
        if (attributes.containsKey("sub")) {
            user.setId(attributes.get("sub").toString());
        }

        // Attribut "given_name" ou "nickname" pour le prénom
        if (attributes.containsKey("given_name")) {
            user.setFirstName(attributes.get("given_name").toString());
        } else if (attributes.containsKey("nickname")) {
            user.setFirstName(attributes.get("nickname").toString());
        }

        // Nom de famille
        if (attributes.containsKey("family_name")) {
            user.setLastName(attributes.get("family_name").toString());
        }

        // Email
        if (attributes.containsKey("email")) {
            user.setEmail(attributes.get("email").toString());
        }

        // Met à jour la date de dernière connexion
        user.setLastSeen(LocalDateTime.now());

        return user;
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .lastSeen(user.getLastSeen())
                .isOnline(user.isOnline())
                .build();
    }
}

