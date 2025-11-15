package com.batuhan.emg_api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/accountFallback")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> accountFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "Account Service");
        response.put("status", "Circuit Open");
        response.put("message", "Account Service is currently unavailable. Please try again later.");
        return Mono.just(response);
    }

    @GetMapping("/fallback/productFallback")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> productFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "Product Service");
        response.put("status", "Circuit Open");
        response.put("message", "Product Service is currently unavailable. Please try again later.");
        return Mono.just(response);
    }
}