package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("dashboard.html")
    public String CheckinCheckout() {
        return "checkin-checkout"; // nếu có download.html
    }
    @GetMapping("/contract-management")
    public String ContractManagement() {
        return "contract-management"; // nếu có download.html
    }
}
