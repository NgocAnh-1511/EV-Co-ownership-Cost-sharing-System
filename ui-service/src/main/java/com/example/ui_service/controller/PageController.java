package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin-schedule.html")
    public String adminSchedule() {
        return "admin-schedule"; // trả về file trong templates/
    }
}
