package com.example.financial_reporting_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration conf = new CorsConfiguration();
                    // Cho phép UI (Cổng 8080) gọi
                    conf.setAllowedOrigins(List.of("http://localhost:8080"));
                    conf.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    conf.setAllowedHeaders(List.of("*"));
                    conf.setAllowCredentials(true);
                    return conf;
                }))
                .authorizeHttpRequests(auth -> auth
                        // Tất cả API báo cáo đều yêu cầu xác thực
                        .requestMatchers("/api/reports/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}