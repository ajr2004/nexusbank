package com.nexusbank.identity_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexusbank.identity_service.dto.TransferRequest;
import com.nexusbank.identity_service.model.Account;
import com.nexusbank.identity_service.service.AccountService;
import com.nexusbank.identity_service.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    // 1. Create a new Account
    @PostMapping("/{userId}")
    public ResponseEntity<Account> createAccount(
            @PathVariable Long userId,
            @RequestParam Account.AccountType type) {

        return ResponseEntity.ok(
                accountService.createAccount(userId, type)
        );
    }

    // 2. Perform a transfer
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestBody TransferRequest request) {

        transactionService.transferFunds(request);
        return ResponseEntity.ok("Transfer Successful!");
    }

    // 3. View all accounts for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUser(
            @PathVariable Long userId) {

        List<Account> accounts =
                accountService.getAccountsByUserId(userId);

        return ResponseEntity.ok(accounts);
    }
}