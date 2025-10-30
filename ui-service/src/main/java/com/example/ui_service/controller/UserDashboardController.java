package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for User Dashboard
 */
@Controller
@RequestMapping("/user")
public class UserDashboardController {

    /**
     * User Dashboard page
     * GET /user
     */
    @GetMapping
    public String userDashboard() {
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

