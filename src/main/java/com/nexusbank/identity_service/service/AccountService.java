package com.nexusbank.identity_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusbank.identity_service.model.Account;
import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.AccountRepository;
import com.nexusbank.identity_service.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountService(
            UserRepository userRepository,
            AccountRepository accountRepository) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public Account createAccount(
            Long userId,
            Account.AccountType type) {

        // 1. Fetch the user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        // 2. Generate account number
        String newAccountNumber = generateUniqueAccountNumber();

        // 3. Build account object
        Account account = Account.builder()
                .accountNumber(newAccountNumber)
                .accountType(type)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found!");
        }

        return accountRepository.findByUserUserId(userId);
    }

    private String generateUniqueAccountNumber() {

        // NB + current timestamp (last digits)
        return "NB" + (System.currentTimeMillis() % 10_00_00_00_000L);
    }
}