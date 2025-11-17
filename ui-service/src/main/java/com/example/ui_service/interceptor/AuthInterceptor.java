package com.example.ui_service.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * AuthInterceptor - DEPRECATED
 * 
 * This interceptor is no longer used for authentication.
 * Authentication is now handled by Spring Security (SecurityConfig.java)
 * via CookieAuthenticationFilter.
 * 
 * Kept for reference only.
 */
// @Component  // Commented out - not used anymore
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        logger.info("AuthInterceptor checking: {}", requestUri);
        
        // Check if user is authenticated
        boolean isAuthenticated = false;
        
        // Method 1: Check cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    isAuthenticated = true;
                    break;
                }
            }
        }
        
        // Method 2: Check localStorage (via session attribute set by frontend)
        // This is a backup in case cookie is not available
        if (!isAuthenticated) {
            Object userIdAttr = request.getSession().getAttribute("userId");
            if (userIdAttr != null) {
                isAuthenticated = true;
            }
        }
        
        if (!isAuthenticated) {
            logger.info("User not authenticated, redirecting to login from: {}", requestUri);
            
            // Store the original URL they wanted to access
            String requestedUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestedUrl += "?" + request.getQueryString();
            }
            
            // Redirect to login page with the original URL as parameter
            try {
                String encodedUrl = java.net.URLEncoder.encode(requestedUrl, "UTF-8");
                response.sendRedirect("/login?redirect=" + encodedUrl);
            } catch (Exception e) {
                response.sendRedirect("/login");
            }
            return false;
        }
        
        logger.info("User authenticated, allowing access to: {}", requestUri);
        return true;
    }
}

