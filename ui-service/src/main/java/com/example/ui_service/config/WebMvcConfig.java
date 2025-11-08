package com.example.ui_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig - Configuration for Spring MVC
 * 
 * Note: Authentication is now handled by Spring Security (SecurityConfig.java)
 * instead of using interceptors. The CookieAuthenticationFilter validates
 * user authentication via cookies.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Authentication handling moved to SecurityConfig
    // No interceptors needed for authentication
}

