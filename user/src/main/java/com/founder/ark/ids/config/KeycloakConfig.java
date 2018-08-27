package com.founder.ark.ids.config;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${ids.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${ids.keycloak.admin.username:admin}")
    private String username;
    @Value("${ids.keycloak.admin.password:admin}")
    private String password;
    private String realm = "master";
    private String clientId = "admin-cli";

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .username(username)
                .password(password)
                .clientId(clientId)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(20).build()).build();
    }


}
