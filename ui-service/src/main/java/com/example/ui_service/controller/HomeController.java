package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index", "/home"})
    public String home() {
        return "redirect:/dashboard";  // Redirect to dashboard
    }
    
    @GetMapping({"/about"})
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/story")
    public String story() {
        return "story";
    }

    @GetMapping("/ride")
    public String ride() {
        return "ride";
    }

    @GetMapping("/download")
    public String download() {
        return "download";
    }
}
