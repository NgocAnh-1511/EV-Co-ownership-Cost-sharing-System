package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    /**
     * Redirect root path to auth login
     */
    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/auth/login";
    }
    
    /**
     * Redirect /dashboard to admin dashboard
     */
    @GetMapping("/dashboard")
    public String redirectDashboard() {
        return "redirect:/admin";
    }
}