package com.nexusbank.loan_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusbank.loan_service.client.BankingClient;
import com.nexusbank.loan_service.client.IdentityClient;
import com.nexusbank.loan_service.dto.AccountResponse;
import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.dto.LoanRequestDto;
import com.nexusbank.loan_service.dto.LoanResponseDto;
import com.nexusbank.loan_service.dto.UserDto;
import com.nexusbank.loan_service.model.Loan;
import com.nexusbank.loan_service.model.Loan.LoanStatus;
import com.nexusbank.loan_service.repository.LoanRepository;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final EmiService emiService;
    private final IdentityClient identityClient;
    private final BankingClient bankingClient;
    
    // =========================================
    // 1️⃣ APPLY LOAN
    // =========================================
    public LoanResponseDto applyLoan(
            LoanRequestDto dto,
            Long userId) {

        
        ResponseEntity<List<AccountResponse>> response = 
                bankingClient.getAccountsByUserId(userId);
        List<AccountResponse> userAccounts = response.getBody();

        // Check if the user actually owns the account number passed in the payload
        boolean ownsAccount = userAccounts != null && userAccounts.stream()
                .anyMatch(acc -> acc.accountNumber().equals(dto.accountNumber()));

        if (!ownsAccount) {
            throw new IllegalArgumentException("Application Rejected: The provided account number does not belong to the authenticated user profile.");
        }
        
        Loan loan = Loan.builder()
                .userId(userId)
                .accountNumber(dto.accountNumber())
                .loanType(dto.loanType())
                .amount(dto.amount())
                .tenureMonths(dto.tenureMonths())
                .interestRate(10.0)
                .status(LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Loan savedLoan = loanRepository.save(loan);

        return mapToResponseDto(savedLoan);
    }

    // =========================================
    // 2️⃣ GET ALL USER LOANS
    // =========================================
    public List<LoanResponseDto> getLoansByUser(Long userId) {

        return loanRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // =========================================
    // 3️⃣ GET SINGLE LOAN
    // =========================================
    public LoanResponseDto getLoan(
            Long loanId,
            Long userId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (!loan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return mapToResponseDto(loan);
    }

    // =========================================
    // 4️⃣ UPDATE LOAN STATUS
    // =========================================
    // 🔒 Enforce ACID properties across distributed boundaries
    @Transactional(rollbackFor = Exception.class)
    public void updateLoanStatus(Long loanId, String status) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        LoanStatus newStatus;
        try {
            newStatus = LoanStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid loan status: " + status);
        }

        // 🛡️ Business Rule: Guard state machine against double approval/disbursement
        if (loan.getStatus() == LoanStatus.APPROVED) {
            throw new IllegalStateException("Rejection: This loan has already been approved and settled.");
        }

        // Update state parameters
        loan.setStatus(newStatus);
        loan.setApprovedAt(java.time.LocalDateTime.now());
        loanRepository.save(loan);

        // Generate EMI schedule and disburse cash only if approved
        if (newStatus == LoanStatus.APPROVED) {
            
            // 1. Compile the True Amortized Database Layout Matrix
            // (Uses your updated EmiService with target account context strings)
            emiService.generateSchedule(loan, "APPROVED_STATUS_GENERATED");

         
            // 2. Trigger Synchronous Microservices Ledger Injection via Feign Client
            try {
                bankingClient.disburseLoanInternal(loan.getAccountNumber(), loan.getAmount());
            } catch (Exception ex) {
                // 🚨 CRITICAL: Cross-service fallback safety valve
                // If banking-service fails, throwing a RuntimeException triggers a 
                // total rollback of the database transaction, reverting the loan state back to PENDING.
                throw new RuntimeException("Distributed transaction failure: Core Banking settlement engine was unreachable or declined disbursement. Reverting credit contract updates.");
            }
        }
    }

    // =========================================
    // 5️⃣ GET EMI SCHEDULE
    // =========================================
    public List<EmiDto> getEmis(
            Long loanId,
            Long userId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (!loan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return emiService.getEmisByLoanId(loanId);
    }

    // =========================================
    // 6️⃣ GET LOANS USING JWT TOKEN
    // =========================================
    public List<LoanResponseDto> getLoansByUsername(
            String token) {

        // Fetch logged-in user
        UserDto user =
                identityClient.getCurrentUser(token);

        // Fetch loans
        List<Loan> loans =
                loanRepository.findByUserId(user.id());

        return loans.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // =========================================
    // 🔁 ENTITY → DTO MAPPER
    // =========================================
    private LoanResponseDto mapToResponseDto(
            Loan loan) {

        return new LoanResponseDto(

                loan.getId(),
                loan.getUserId(),
                loan.getAccountNumber(),

                loan.getLoanType(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),

                loan.getStatus().name()
        );
    }
}