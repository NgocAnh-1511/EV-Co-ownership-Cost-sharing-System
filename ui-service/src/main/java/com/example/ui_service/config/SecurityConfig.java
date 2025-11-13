package com.example.ui_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())               // Tắt CSRF (cho test)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()           // Cho phép tất cả URL truy cập
                )
                .formLogin(login -> login.disable())        // Tắt form login
                .httpBasic(basic -> basic.disable());       // Tắt basic auth
        return http.build();
    }
}