package com.founder.ark.ids.config;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
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
    @Value("${avatar.keycloak.serverUrl:https://sso.example.com/auth}")
    private String serverUrl;
    @Value("${avatar.keycloak.admin-realm:master}")
    private String realm;
    @Value("${avatar.keycloak.client:admin-cli}")
    private String client;
    @Value("${avatar.keycloak.username:username}")
    private String username;
    @Value("${avatar.keycloak.password:password}")
    private String password;

    @RefreshScope
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .username(username)
                .password(password)
                .clientId(client)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(20).build())
                .build();
    }


}
