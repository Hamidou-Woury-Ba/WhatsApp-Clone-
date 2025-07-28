package com.hamidou.whatsappclone.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketService implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Active un broker simple embarqué avec le préfixe /user pour envoyer les messages aux utilisateurs
        registry.enableSimpleBroker("/user");

        // Les messages envoyés depuis le client doivent commencer par /app pour être routés vers les méthodes contrôleurs (ex: @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Permet de faire des envois spécifiques à un utilisateur (ex: /users/{username})
        registry.setUserDestinationPrefix("/users");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws") // Le client WebSocket se connectera à /ws
                .setAllowedOrigins("http://localhost:4200") // Autorise les connexions depuis ton frontend Angular local
                .withSockJS(); // Active SockJS pour fallback HTTP si WebSocket n’est pas disponible
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // Ajoute un résolveur pour injecter l’utilisateur connecté dans les contrôleurs WebSocket
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }


    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Définit le type de contenu par défaut à JSON
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MediaType.APPLICATION_JSON);

        // Utilise Jackson pour convertir les objets Java en JSON et vice versa
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper()); // Le convertisseur utilise Jackson
        converter.setContentTypeResolver(resolver); // Définit le type MIME comme JSON

        messageConverters.add(converter); // Ajoute le convertisseur à la configuration

        return false; // Laisse Spring utiliser ses convertisseurs par défaut en plus
    }

}
