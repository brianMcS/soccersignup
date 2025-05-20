package com.soccersignup.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class HealthController {
    
    @GetMapping("health")
    public String healthCheck() {
        return "OK";
    }
    
}
