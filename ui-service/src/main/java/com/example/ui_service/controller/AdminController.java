package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Admin Dashboard
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * Admin Dashboard page
     * GET /admin
     */
    @GetMapping
    public String adminDashboard() {
        return "admin-dashboard";
    }
    
    /**
     * Redirect /admin/dashboard to /admin
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin";
    }
}

