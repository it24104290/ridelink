package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.AccountResponse;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UpdateStatusRequest;
import com.ridelink.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    @Operation(summary = "View the authenticated account profile")
    public AccountResponse me(Authentication authentication) {
        return accountService.getMe(authentication.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated account profile")
    public AccountResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return accountService.updateMe(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by id")
    public AccountResponse getById(@PathVariable String id, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return accountService.getById(id, authentication.getName(), role);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or suspend an account")
    public AccountResponse updateStatus(@PathVariable String id, @Valid @RequestBody UpdateStatusRequest request) {
        return accountService.updateStatus(id, request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all accounts")
    public List<AccountResponse> list() {
        return accountService.listAll();
    }
}
