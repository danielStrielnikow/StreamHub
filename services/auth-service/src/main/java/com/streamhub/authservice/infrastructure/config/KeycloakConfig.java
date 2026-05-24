package com.streamhub.authservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakConfig {
    
    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin-user}")
    private String adminUser;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUser)
                .password(adminPassword)
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
