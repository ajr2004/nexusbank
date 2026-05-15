package com.nexusbank.loan_service.controller;

import com.nexusbank.loan_service.client.IdentityClient;
import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.dto.LoanRequestDto;
import com.nexusbank.loan_service.dto.LoanResponseDto;
import com.nexusbank.loan_service.dto.LoanStatusUpdateDto;
import com.nexusbank.loan_service.dto.UserDto;
import com.nexusbank.loan_service.service.LoanService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final IdentityClient identityClient;

    // 1️⃣ Apply Loan
    @PostMapping
    public LoanResponseDto applyLoan(
            @Valid @RequestBody LoanRequestDto request,
            @RequestHeader("Authorization") String token) {

        UserDto user = identityClient.getCurrentUser(token);

        return loanService.applyLoan(request, user.getId());
    }

    // 2️⃣ Get all loans of logged-in user
    @GetMapping("/me")
    public List<LoanResponseDto> getMyLoans(
            @RequestHeader("Authorization") String token) {

        UserDto user = identityClient.getCurrentUser(token);

        return loanService.getLoansByUser(user.getId());
    }

    // 3️⃣ Get single loan
    @GetMapping("/{loanId}")
    public LoanResponseDto getLoan(
            @PathVariable Long loanId,
            @RequestHeader("Authorization") String token) {

        UserDto user = identityClient.getCurrentUser(token);

        return loanService.getLoan(loanId, user.getId());
    }

    // 4️⃣ Get EMI schedule
    @GetMapping("/{loanId}/emis")
    public List<EmiDto> getEmis(
            @PathVariable Long loanId,
            @RequestHeader("Authorization") String token) {

        UserDto user = identityClient.getCurrentUser(token);

        return loanService.getEmis(loanId, user.getId());
    }

    // 5️⃣ Admin: Update Loan Status
    @PutMapping("/{loanId}/status")
    public String updateLoanStatus(
            @PathVariable Long loanId,
            @RequestBody LoanStatusUpdateDto request) {

        loanService.updateLoanStatus(loanId, request.getStatus());

        return "Loan status updated successfully";
    }
}