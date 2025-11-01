package com.example.ui_service.config;

import com.example.ui_service.filter.CookieAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Spring Security...");
        
        http
                .authorizeHttpRequests(auth -> auth
                        // Các routes công khai
                        .requestMatchers("/", "/login", "/admin/**", "/api/auth/**",
                                       "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Các routes cần authentication - PHẢI CHECK SAU
                        .requestMatchers("/dashboard", "/reservations/**", 
                                       "/vehicles/**", "/reports/**").authenticated()
                        // Các request khác - DENY BY DEFAULT
                        .anyRequest().denyAll()
                )
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .httpBasic(httpBasic -> httpBasic.disable()) // Tắt HTTP Basic Auth popup
                .formLogin(login -> login.disable()) // Tắt form login mặc định
                .logout(logout -> logout.disable()) // Tắt logout mặc định
                .anonymous(anonymous -> anonymous.disable()) // Tắt anonymous authentication
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // FORCE redirect - NO popup
                            logger.warn("⚠️ Authentication required for: {}", request.getRequestURI());
                            
                            // Set response headers to prevent browser auth popup
                            response.setStatus(HttpServletResponse.SC_FOUND); // 302
                            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                            response.setHeader("Pragma", "no-cache");
                            response.setHeader("Expires", "0");
                            
                            String requestedUrl = request.getRequestURI();
                            if (request.getQueryString() != null) {
                                requestedUrl += "?" + request.getQueryString();
                            }
                            String encodedUrl = java.net.URLEncoder.encode(requestedUrl, "UTF-8");
                            response.sendRedirect("/login?redirect=" + encodedUrl);
                        })
                )
                // Add cookie authentication filter
                .addFilterBefore(cookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("Spring Security configured successfully");
        return http.build();
    }
}
