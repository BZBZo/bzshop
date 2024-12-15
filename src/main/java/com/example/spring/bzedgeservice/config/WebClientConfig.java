package com.example.spring.bzedgeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${bzbzo.bz-auth-service-url}")
    private String bzAuthServiceUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient authClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(bzAuthServiceUrl).build();
    }
}