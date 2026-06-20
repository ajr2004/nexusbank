package com.nexusbank.loan_service.client;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.nexusbank.loan_service.dto.AccountResponse;

@FeignClient(name = "banking-service")
public interface BankingClient {
    @PostMapping("/api/accounts/internal/disburse")
    ResponseEntity<Void> disburseLoanInternal(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("amount") BigDecimal amount
    );

// 🔍 ADD THIS: Explicit Feign mapping to look up user accounts by ID
    @GetMapping("/api/accounts/internal/user/{userId}")
    ResponseEntity<List<AccountResponse>> getAccountsByUserId(@PathVariable("userId") Long userId);

    // 💸 ADD THIS inside your existing BankingClient interface:
    @PostMapping("/api/accounts/internal/debit")
    org.springframework.http.ResponseEntity<Void> debitAccountInternal(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("amount") java.math.BigDecimal amount
    );
}