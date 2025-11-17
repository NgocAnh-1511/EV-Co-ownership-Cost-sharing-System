package com.example.ui_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class CookieAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CookieAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        logger.info("CookieAuthenticationFilter processing: {}", requestUri);
        
        String userId = null;
        String role = "ROLE_USER";
        
        // Method 1: Check Authorization header (for admin)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Found Bearer token in Authorization header");
            // For now, just mark as authenticated (you can validate JWT later)
            userId = "admin";
            role = "ROLE_ADMIN";
        }
        
        // Method 2: Check cookie (for regular users)
        if (userId == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("userId".equals(cookie.getName())) {
                        userId = cookie.getValue();
                        logger.info("Found userId cookie: {}", userId);
                        break;
                    }
                }
            }
        }
        
        // If userId found, set authentication in SecurityContext
        if (userId != null && !userId.isEmpty()) {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userId, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority(role))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Authentication set for user: {} with role: {}", userId, role);
        } else {
            logger.info("No authentication found - user not authenticated");
        }
        
        filterChain.doFilter(request, response);
    }
}

