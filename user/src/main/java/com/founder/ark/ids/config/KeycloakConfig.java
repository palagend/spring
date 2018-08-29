package com.founder.ark.ids.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
@Slf4j
public class KeycloakConfig {
    @Value("${ids.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${ids.keycloak.admin.username:admin}")
    private String username;
    @Value("${ids.keycloak.admin.password:admin}")
    private String password;
    private String realm = "master";
    private String clientId = "admin-cli";

    @RefreshScope
    @Bean
    public Keycloak keycloak() {
        log.info("Keycloak Server URL is: {}", serverUrl);
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .username(username)
                .password(password)
                .clientId(clientId)
                .build();
    }


}
