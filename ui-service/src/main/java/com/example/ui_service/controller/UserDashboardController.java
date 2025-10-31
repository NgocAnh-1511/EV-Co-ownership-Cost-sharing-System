package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for User Dashboard
 */
@Controller
@RequestMapping("/user")
public class UserDashboardController {

    /**
     * User Dashboard page
     * GET /user?userId=X
     * For demo: accept userId as query param
     * In production: get from session/authentication
     */
    @GetMapping
    public String userDashboard(
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
            Model model) {
        
        // Add user info to model
        model.addAttribute("userId", userId);
        model.addAttribute("userName", "User #" + userId);
        model.addAttribute("userEmail", "user" + userId + "@example.com");
        
        return "user-dashboard";
    }
    
    /**
     * Redirect /user/dashboard to /user
     * GET /user/dashboard
     */
    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        return "redirect:/user";
    }
}

