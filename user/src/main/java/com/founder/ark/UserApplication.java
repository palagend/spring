package com.founder.ark;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPool;

import java.util.Properties;

/**
 * IDS核心
 *
 * @author huyh
 */
@EnableFeignClients//声明式接口调用
@SpringCloudApplication
public class UserApplication {
    private String realm = "master";
    private String clientId = "admin-cli";
    @Value("${ids.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${ids.keycloak.admin.username:admin}")
    private String username;
    @Value("${ids.keycloak.admin.password:admin}")
    private String password;
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private String redisPort;


    /**
     * 启动
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    public JedisPool jedisPool() {
        Properties props = new Properties();
        props.setProperty("redisHost", this.redisHost);
        props.setProperty("redisPort", this.redisPort);
        String host = props.getProperty("redisHost", "172.19.58.206");
        String port = props.getProperty("redisPort", "6379");
        JedisPool jedisPool = new JedisPool(host, new Integer(port));
        jedisPool.init();
        return jedisPool;
    }

    @Bean
    public Keycloak keycloak() {
        Properties props = new Properties();
        props.setProperty("ids.keycloak.serverUrl", this.serverUrl);
        props.setProperty("ids.keycloak.admin.username", this.username);
        props.setProperty("ids.keycloak.admin.password", this.password);
        String serverUrl = props.getProperty("ids.keycloak.serverUrl");
        String username = props.getProperty("ids.keycloak.admin.username", "admin");
        String password = props.getProperty("ids.keycloak.admin.password", "admin");
        return Keycloak.getInstance(serverUrl, realm, username, password, clientId);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
