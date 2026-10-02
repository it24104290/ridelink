package com.ridelink.account_service.service;

import com.ridelink.account_service.domain.Account;
import com.ridelink.account_service.domain.AccountStatus;
import com.ridelink.account_service.domain.Role;
import com.ridelink.account_service.dto.AccountResponse;
import com.ridelink.account_service.dto.AuthResponse;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UpdateStatusRequest;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.repository.AccountRepository;
import com.ridelink.account_service.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin accounts cannot be self-registered");
        }
        if (accountRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }

        Instant now = Instant.now();
        Account account = new Account();
        account.setFullName(request.fullName().trim());
        account.setEmail(request.email().trim().toLowerCase());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setPhone(request.phone().trim());
        account.setRole(request.role());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        Account saved = accountRepository.save(account);
        return toAuth(saved);
    }

    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is suspended");
        }

        return toAuth(account);
    }

    public AccountResponse getById(String id, String callerId, String callerRole) {
        if (!"ADMIN".equals(callerRole) && !"SERVICE".equals(callerRole) && !callerId.equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return toResponse(find(id));
    }

    public AccountResponse getMe(String accountId) {
        return toResponse(find(accountId));
    }

    public AccountResponse updateMe(String accountId, UpdateProfileRequest request) {
        Account account = find(accountId);
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone().trim());
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse updateStatus(String id, UpdateStatusRequest request) {
        Account account = find(id);
        account.setStatus(request.status());
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public List<AccountResponse> listAll() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    private Account find(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AuthResponse toAuth(Account account) {
        String token = jwtService.generateToken(account.getId(), account.getEmail(), account.getRole().name());
        return new AuthResponse(token, "Bearer", account.getId(), account.getEmail(), account.getRole());
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}
