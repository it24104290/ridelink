package com.ridelink.account_service.service;

import com.ridelink.account_service.domain.Account;
import com.ridelink.account_service.domain.AccountStatus;
import com.ridelink.account_service.domain.Role;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.repository.AccountRepository;
import com.ridelink.account_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerCreatesActivePassenger() {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe", "jane@ridelink.local", "Password123!", "0771112233", Role.PASSENGER);

        when(accountRepository.existsByEmail("jane@ridelink.local")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedHash");
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("mockJwt");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId("acc-jane-001");
            return saved;
        });

        var response = accountService.register(request);

        assertNotNull(response);
        assertEquals("mockJwt", response.token());
        assertEquals("jane@ridelink.local", response.email());
        assertEquals(Role.PASSENGER, response.role());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void registerRejectsAdminSelfRegistration() {
        RegisterRequest request = new RegisterRequest(
                "Admin User", "admin@ridelink.local", "Password123!", "0771112233", Role.ADMIN);

        ApiException ex = assertThrows(ApiException.class, () -> accountService.register(request));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void loginRejectsSuspendedAccount() {
        Account suspended = new Account();
        suspended.setId("acc-susp");
        suspended.setEmail("susp@ridelink.local");
        suspended.setPasswordHash("encodedHash");
        suspended.setStatus(AccountStatus.SUSPENDED);
        suspended.setRole(Role.PASSENGER);

        when(accountRepository.findByEmail("susp@ridelink.local")).thenReturn(Optional.of(suspended));
        when(passwordEncoder.matches("Password123!", "encodedHash")).thenReturn(true);

        LoginRequest req = new LoginRequest("susp@ridelink.local", "Password123!");
        ApiException ex = assertThrows(ApiException.class, () -> accountService.login(req));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}
