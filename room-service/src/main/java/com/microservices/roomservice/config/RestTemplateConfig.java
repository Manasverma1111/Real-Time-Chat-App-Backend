package com.microservices.roomservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

//    RestTemplateConfig is a configuration class that defines a RestTemplate bean
//    for making HTTP requests to other services in the application.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}