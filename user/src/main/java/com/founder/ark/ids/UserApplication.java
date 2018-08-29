package com.founder.ark.ids;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;

import java.util.Properties;

@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private String redisPort;


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
}
