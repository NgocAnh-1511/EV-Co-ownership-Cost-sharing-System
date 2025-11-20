package com.example.reservationadminservice.controller;

import com.example.reservationadminservice.service.BookingUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final BookingUserService bookingUserService;
    
    public TestController(BookingUserService bookingUserService) {
        this.bookingUserService = bookingUserService;
    }
    
    @GetMapping("/user/{userId}")
    public String testGetUser(@PathVariable Long userId) {
        String fullName = bookingUserService.getUserFullName(userId);
        return "User ID: " + userId + ", Full Name: " + (fullName != null ? fullName : "NOT FOUND");
    }
}

