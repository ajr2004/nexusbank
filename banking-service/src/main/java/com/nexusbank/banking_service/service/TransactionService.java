package com.nexusbank.banking_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusbank.banking_service.client.IdentityClient;
import com.nexusbank.banking_service.dto.TransactionResponseDto;
import com.nexusbank.banking_service.dto.TransferRequest;
import com.nexusbank.banking_service.dto.UserDto;
import com.nexusbank.banking_service.model.Account;
import com.nexusbank.banking_service.model.Transaction;
import com.nexusbank.banking_service.repository.AccountRepository;
import com.nexusbank.banking_service.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdentityClient identityClient;

    @Transactional
    public void transferFunds(TransferRequest request) {

        // 1. Fetch source account
        Account sourceAccount = accountRepository
                .findByAccountNumberWithLock(request.fromAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Source account not found!"));

        // 2. Fetch target account
        Account targetAccount = accountRepository
                .findByAccountNumberWithLock(request.toAccountNumber())
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

        Transaction creditRecord = Transaction.builder()
                .amount(request.amount())
                .type(Transaction.TransactionType.CREDIT)
                .category(request.category() != null ? request.category() : "General Transfer")
                .description("Transfer inbound from " + sourceAccount.getAccountNumber())
                .account(targetAccount)
                .targetAccountNumber(sourceAccount.getAccountNumber())
                .build();

transactionRepository.save(creditRecord); // Save alongside debitRecord

        // 6. Save everything
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        transactionRepository.save(debitRecord);
    }

    public List<TransactionResponseDto> getTransactionsByAccount(
                Long accountId,
                String token
        ) {

        // ✅ Get logged-in user
        UserDto user =
                identityClient.getCurrentUser(token);

        // ✅ Fetch account
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Account not found"
                        ));

        // ✅ Ownership validation
        if (!account.getUserId().equals(user.id())) {

                throw new RuntimeException(
                        "Unauthorized access to account"
                );
        }

        // ✅ Return transactions
        return transactionRepository
        .findByAccount_AccountId(accountId)
        .stream()
        .map(transaction -> new TransactionResponseDto(

                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getTargetAccountNumber(),
                transaction.getTimestamp()

        ))
        .toList();
        }
}