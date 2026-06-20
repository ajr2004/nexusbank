package com.nexusbank.banking_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexusbank.banking_service.dto.AccountResponse;
import com.nexusbank.banking_service.dto.DepositRequest;
import com.nexusbank.banking_service.dto.TransactionResponseDto;
import com.nexusbank.banking_service.dto.TransferRequest;
import com.nexusbank.banking_service.dto.WithdrawRequest;
import com.nexusbank.banking_service.service.AccountService;
import com.nexusbank.banking_service.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    // 1️⃣ Create Account
    @PostMapping("/{userId}")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long userId,
            @RequestParam com.nexusbank.banking_service.model.Account.AccountType type
    ) {

        return ResponseEntity.ok(
                accountService.createAccount(userId, type)
        );
    }

    // 2️⃣ Transfer Money
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestBody TransferRequest request
    ) {

        transactionService.transferFunds(request);

        return ResponseEntity.ok(
                "Transfer Successful!"
        );
    }

    // 3️⃣ Get User Accounts
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                accountService.getAccountsByUserId(userId)
        );
    }

    // 4️⃣ Deposit
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/deposit")
    public AccountResponse deposit(
            @RequestBody DepositRequest request
    ) {

        return accountService.deposit(
                request.accountId(),
                request.amount()
        );
    }

    // 5️⃣ Withdraw
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/withdraw")
    public AccountResponse withdraw(
            @RequestBody WithdrawRequest request
    ) {

        return accountService.withdraw(
                request.accountId(),
                request.amount()
        );
    }

    // 6️⃣ Transactions
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{accountId}/transactions")
    public List<TransactionResponseDto> getTransactions(
            @PathVariable Long accountId,
            @RequestHeader("Authorization") String token
    ) {

        return transactionService
                .getTransactionsByAccount(
                        accountId,
                        token
                );
    }
    
    @PostMapping("/internal/disburse")
        public org.springframework.http.ResponseEntity<Void> disburseLoanInternal(
                @RequestParam String accountNumber,
                @RequestParam java.math.BigDecimal amount) {
        accountService.disburseLoanFundsInternal(accountNumber, amount);
        return org.springframework.http.ResponseEntity.ok().build();
        }
     
    // 🔓 Secure internal path for other microservices (bypasses customer JWT checks)
    @GetMapping("/internal/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserIdInternal(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    // 🔓 Secure internal path for background system components
    @PostMapping("/internal/debit")
    public ResponseEntity<Void> debitAccountInternal(
            @RequestParam String accountNumber,
            @RequestParam java.math.BigDecimal amount) {
        
        accountService.debitAccountInternal(accountNumber, amount);
        return ResponseEntity.ok().build();
    }
    
}