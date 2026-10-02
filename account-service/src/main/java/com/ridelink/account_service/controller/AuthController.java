package com.ridelink.account_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.account_service.dto.AuthResponse;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a passenger or driver account")

    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        
        return accountService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")

    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        
        return accountService.login(request);
    }
}
