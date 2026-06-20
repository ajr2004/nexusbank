package com.nexusbank.banking_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusbank.banking_service.client.IdentityClient;
import com.nexusbank.banking_service.dto.AccountResponse;
import com.nexusbank.banking_service.model.Account;
import com.nexusbank.banking_service.model.Transaction;
import com.nexusbank.banking_service.repository.AccountRepository;
import com.nexusbank.banking_service.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final IdentityClient identityClient;
    private final TransactionRepository transactionRepository;

    // 1️⃣ Create Account
    public AccountResponse createAccount(
            Long userId,
            Account.AccountType type
    ) {

        // Validate user
        boolean userExists =
                identityClient.checkUserExists(userId);

        if (!userExists) {

            throw new RuntimeException(
                    "User with ID "
                    + userId
                    + " does not exist."
            );
        }

        // Generate account number
        String newAccountNumber =
                generateUniqueAccountNumber();

        // Build account
        Account account = Account.builder()
                .accountNumber(newAccountNumber)
                .accountType(type)
                .balance(BigDecimal.ZERO)
                .userId(userId)
                .build();

        Account savedAccount =
                accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // 2️⃣ Get User Accounts
    public List<AccountResponse> getAccountsByUserId(
            Long userId
    ) {

        return accountRepository
                .findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 3️⃣ Deposit
    public AccountResponse deposit(
            Long accountId,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Deposit amount must be greater than zero"
            );
        }

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                ));

        account.setBalance(
                account.getBalance().add(amount)
        );

        Transaction transaction =
                Transaction.builder()
                        .amount(amount)
                        .type(Transaction.TransactionType.CREDIT)
                        .category("Deposit")
                        .description(
                                "Money deposited into account"
                        )
                        .account(account)
                        .build();

        transactionRepository.save(transaction);

        Account updatedAccount =
                accountRepository.save(account);

        return mapToResponse(updatedAccount);
    }

    // 4️⃣ Withdraw
    public AccountResponse withdraw(
            Long accountId,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Withdraw amount must be greater than zero"
            );
        }

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                ));

        if (account.getBalance()
                .compareTo(amount) < 0) {

            throw new RuntimeException(
                    "Insufficient balance"
            );
        }

        account.setBalance(
                account.getBalance()
                        .subtract(amount)
        );

        Transaction transaction =
                Transaction.builder()
                        .amount(amount)
                        .type(Transaction.TransactionType.DEBIT)
                        .category("Withdrawal")
                        .description(
                                "Money withdrawn from account"
                        )
                        .account(account)
                        .build();

        transactionRepository.save(transaction);

        Account updatedAccount =
                accountRepository.save(account);

        return mapToResponse(updatedAccount);
    }

    // ✅ Mapper
    private AccountResponse mapToResponse(
            Account account
    ) {

        return new AccountResponse(

                account.getAccountId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getBalance()
        );
    }

    // ✅ Generate Account Number
    private String generateUniqueAccountNumber() {

        return "NB"
                + (System.currentTimeMillis()
                % 10_00_00_00_000L);
    }

    @Transactional
        public void disburseLoanFundsInternal(String accountNumber, java.math.BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Target payout account missing."));
        account.setBalance(account.getBalance().add(amount));

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .type(Transaction.TransactionType.CREDIT)
                .category("Loan Disbursement")
                .description("Disbursement payout transfer")
                .account(account)
                .build();

        transactionRepository.save(transaction);
        accountRepository.save(account);
        }

        @Transactional
        public void debitAccountInternal(String accountNumber, java.math.BigDecimal amount) {
                // 1. Fetch the account using our strict safety lock
                Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                        .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));

                // 2. Safety Check: Does the user have enough money to cover the EMI?
                if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds for automated installment collection.");
                }

                // 3. Deduct the funds from the ledger balance
                account.setBalance(account.getBalance().subtract(amount));
                accountRepository.save(account);
                log.info("Successfully debited ₹{} from account {} via Automated Billing System.", amount, accountNumber);

                // 4. CREATE THE TRANSACTION LOG ENTRY BELOW:
                Transaction tx = new Transaction();
                tx.setAccount(account);                       // Set parent account mapping
                tx.setAmount(amount);                         // Set the EMI amount 
                tx.setType(Transaction.TransactionType.DEBIT); // Set Enum type token
                tx.setCategory("LOAN_EMI");                   // Set category grouping 
                tx.setDescription("Automated Loan EMI Deduction"); 
                tx.setTargetAccountNumber(accountNumber);     // Tracks the originating account target
                
                // Save the transaction record to banking_db.transactions
                transactionRepository.save(tx);
                log.info("Transaction history record generated cleanly for EMI ID allocation matching account: {}", accountNumber);
        }

}