package com.founder.ark.ids.service.core.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by cheng.ly on 2018/4/9.
 */
@Configuration
public class ControllerInterceptorMVC extends WebMvcConfigurerAdapter {

    @Bean
    public ControllerInterceptor controllerInterceptor(){
        return new ControllerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
