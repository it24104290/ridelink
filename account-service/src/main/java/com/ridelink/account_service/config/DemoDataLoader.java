package com.ridelink.account_service.config;

import com.ridelink.account_service.domain.Account;
import com.ridelink.account_service.domain.AccountStatus;
import com.ridelink.account_service.domain.Role;
import com.ridelink.account_service.repository.AccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "ridelink.seed-demo-data", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accountRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        accountRepository.saveAll(List.of(
                account("acc-admin-001", "RideLink Admin", "admin@ridelink.local", Role.ADMIN, now),
                account("acc-passenger-001", "Amal Perera", "passenger@ridelink.local", Role.PASSENGER, now),
                account("acc-driver-001", "Nimal Silva", "driver@ridelink.local", Role.DRIVER, now)
        ));
    }

    private Account account(String id, String name, String email, Role role, Instant now) {
        Account account = new Account();
        account.setId(id);
        account.setFullName(name);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode("Password123!"));
        account.setPhone("0770000000");
        account.setRole(role);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        return account;
    }
}
