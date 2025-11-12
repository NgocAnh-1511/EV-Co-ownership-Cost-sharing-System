package com.example.ui_service.config;

<<<<<<< HEAD
=======
import com.fasterxml.jackson.databind.ObjectMapper;
>>>>>>> d9a78b7886179ffc797ae2e063c9f95a65bf1d5b
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
<<<<<<< HEAD
=======

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
>>>>>>> d9a78b7886179ffc797ae2e063c9f95a65bf1d5b
}
