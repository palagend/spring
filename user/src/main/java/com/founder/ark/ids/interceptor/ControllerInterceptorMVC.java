package com.founder.ark.ids.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by cheng.ly on 2018/4/9.
 */
@Configuration
public class ControllerInterceptorMVC implements WebMvcConfigurer {

    @Bean
    public ControllerInterceptor controllerInterceptor() {
        return new ControllerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerInterceptor()).addPathPatterns("/**");
    }
}
