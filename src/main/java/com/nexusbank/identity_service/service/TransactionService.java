package com.nexusbank.identity_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.nexusbank.identity_service.dto.TransferRequest;
import com.nexusbank.identity_service.model.Account;
import com.nexusbank.identity_service.model.Transaction;
import com.nexusbank.identity_service.repository.AccountRepository;
import com.nexusbank.identity_service.repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transferFunds(TransferRequest request) {

        // 1. Fetch source account
        Account sourceAccount = accountRepository
                .findByAccountNumber(request.fromAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Source account not found!"));

        // 2. Fetch target account
        Account targetAccount = accountRepository
                .findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Target account not found!"));

        // Prevent transfer to same account
        if (sourceAccount.getAccountNumber()
                .equals(targetAccount.getAccountNumber())) {

            throw new RuntimeException(
                    "Cannot transfer money to same account");
        }

        // 3. Check balance
        if (sourceAccount.getBalance()
                .compareTo(request.amount()) < 0) {

            throw new RuntimeException(
                    "Insufficient balance in source account");
        }

        // 4. Update balances
        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.amount()));

        targetAccount.setBalance(
                targetAccount.getBalance()
                        .add(request.amount()));

        // 5. Create transaction record
        Transaction debitRecord = Transaction.builder()
                .amount(request.amount())
                .type(Transaction.TransactionType.TRANSFER)
                .category(
                        request.category() != null
                                ? request.category()
                                : "General Transfer"
                )
                .description(
                        "Transfer to " +
                        targetAccount.getAccountNumber()
                )
                .account(sourceAccount)
                .targetAccountNumber(
                        targetAccount.getAccountNumber()
                )
                .timestamp(LocalDateTime.now())
                .build();

        // 6. Save everything
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        transactionRepository.save(debitRecord);
    }
}