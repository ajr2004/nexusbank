package com.nexusbank.loan_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.dto.LoanRequestDto;
import com.nexusbank.loan_service.dto.LoanResponseDto;
import com.nexusbank.loan_service.model.Loan;
import com.nexusbank.loan_service.model.Loan.LoanStatus;
import com.nexusbank.loan_service.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final EmiService emiService;

    // 1️⃣ APPLY LOAN
    public LoanResponseDto applyLoan(LoanRequestDto dto, Long userId) {

        Loan loan = Loan.builder()
                .userId(userId)
                .loanType(dto.getLoanType())
                .amount(dto.getAmount())
                .tenureMonths(dto.getTenureMonths())
                .interestRate(10.0) // static for now
                .status(LoanStatus.PENDING) // ✅ FIXED
                .createdAt(LocalDateTime.now())
                .build();

        Loan saved = loanRepository.save(loan);

        return mapToDto(saved);
    }

    // 2️⃣ GET USER LOANS
    public List<LoanResponseDto> getLoansByUser(Long userId) {
        return loanRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // 3️⃣ GET BY ID
    public LoanResponseDto getLoan(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToDto(loan);
    }

    // 4️⃣ ADMIN APPROVAL
    public void updateLoanStatus(Long loanId, String status) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // ✅ Convert String → ENUM safely
        LoanStatus newStatus;
        try {
            newStatus = LoanStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid loan status: " + status);
        }

        loan.setStatus(newStatus);
        loanRepository.save(loan);

        // 🔥 Trigger EMI generation only on approval
        if (newStatus == LoanStatus.APPROVED) {
            emiService.generateSchedule(loan);
        }
    }

    // 🔁 MAPPER
    private LoanResponseDto mapToDto(Loan loan) {
        return LoanResponseDto.builder()
                .loanId(loan.getId())
                .userId(loan.getUserId())
                .loanType(loan.getLoanType())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .status(loan.getStatus().name()) // ✅ ENUM → String
                .build();
    }
    public List<EmiDto> getEmis(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return emiService.getEmisByLoanId(loanId);
    }
}