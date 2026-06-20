package com.nexusbank.loan_service.controller;

import com.nexusbank.loan_service.client.IdentityClient;
import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.dto.LoanRequestDto;
import com.nexusbank.loan_service.dto.LoanResponseDto;
import com.nexusbank.loan_service.dto.LoanStatusUpdateDto;
import com.nexusbank.loan_service.dto.UserDto;
import com.nexusbank.loan_service.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final IdentityClient identityClient;

    // =========================================
    // 1️⃣ APPLY LOAN
    // =========================================
    @PostMapping
    public LoanResponseDto applyLoan(

            @Valid @RequestBody LoanRequestDto request,

            @RequestHeader("Authorization")
            String token) {

        UserDto user =
                identityClient.getCurrentUser(token);

        return loanService.applyLoan(
                request,
                user.id()
        );
    }

    // =========================================
    // 2️⃣ GET MY LOANS
    // =========================================
    @GetMapping("/me")
    public List<LoanResponseDto> getMyLoans(

            @RequestHeader("Authorization")
            String token) {

        return loanService.getLoansByUsername(token);
    }

    // =========================================
    // 3️⃣ GET SINGLE LOAN
    // =========================================
    @GetMapping("/{loanId}")
    public LoanResponseDto getLoan(

            @PathVariable Long loanId,

            @RequestHeader("Authorization")
            String token) {

        UserDto user =
                identityClient.getCurrentUser(token);

        return loanService.getLoan(
                loanId,
                user.id()
        );
    }

    // =========================================
    // 4️⃣ GET EMI SCHEDULE
    // =========================================
    @GetMapping("/{loanId}/emis")
    public List<EmiDto> getEmis(

            @PathVariable Long loanId,

            @RequestHeader("Authorization")
            String token) {

        UserDto user =
                identityClient.getCurrentUser(token);

        return loanService.getEmis(
                loanId,
                user.id()
        );
    }

    // =========================================
    // 5️⃣ ADMIN UPDATE LOAN STATUS
    // =========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanId}/status")
    public String updateLoanStatus(

            @PathVariable Long loanId,

            @RequestBody LoanStatusUpdateDto request) {

        loanService.updateLoanStatus(
                loanId,
                request.status()
        );

        return "Loan status updated successfully";
    }
}