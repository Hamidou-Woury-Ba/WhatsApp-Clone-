package com.hamidou.whatsappclone;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@SecurityScheme(
		name = "keycloak", // Nom du schéma de sécurité utilisé dans l'API
		type = SecuritySchemeType.OAUTH2, // Type d'authentification (OAuth2 dans ton cas)
		bearerFormat = "JWT", // Format du token : JSON Web Token
		scheme = "bearer", // Type du schéma dans l’en-tête Authorization : "Bearer ..."
		in = SecuritySchemeIn.HEADER, // Le token est attendu dans les headers HTTP
		flows = @OAuthFlows(
				password = @OAuthFlow(
						authorizationUrl = "http://localhost:9090/realms/master/protocol/openid-connect/auth",
						tokenUrl = "http://localhost:9090/realms/master/protocol/openid-connect/token"
				)
		)
)
public class WhatsAppCloneApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsAppCloneApiApplication.class, args);
	}

}
