package com.nexusbank.banking_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusbank.banking_service.client.IdentityClient;
import com.nexusbank.banking_service.model.Account;
import com.nexusbank.banking_service.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final IdentityClient identityClient;

    public Account createAccount(
            Long userId,
            Account.AccountType type) {

        // 1. Check if user exists in Identity Service
        boolean userExists = identityClient.checkUserExists(userId);
        if (!userExists) {
            throw new RuntimeException("User with ID " + userId + " does not exist.");
        }

     

        // 2. Generate account number
        String newAccountNumber = generateUniqueAccountNumber();

        // 3. Build account object
        Account account = Account.builder()
                .accountNumber(newAccountNumber)
                .accountType(type)
                .balance(BigDecimal.ZERO)
                .userId(userId)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(Long userId) {


        return accountRepository.findByUserId(userId);
    }

    private String generateUniqueAccountNumber() {

        // NB + current timestamp (last digits)
        return "NB" + (System.currentTimeMillis() % 10_00_00_00_000L);
    }
}