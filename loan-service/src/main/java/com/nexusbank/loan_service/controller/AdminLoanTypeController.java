package com.nexusbank.loan_service.controller;

import com.nexusbank.loan_service.model.LoanType;
import com.nexusbank.loan_service.repository.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans/admin/loan-types")
@RequiredArgsConstructor
public class AdminLoanTypeController {

    private final LoanTypeRepository loanTypeRepository;

    @GetMapping
    public ResponseEntity<List<LoanType>> getAll() {
        return ResponseEntity.ok(loanTypeRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<LoanType> create(@Valid @RequestBody LoanType loanType) {
        return ResponseEntity.ok(loanTypeRepository.save(loanType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanType> update(@PathVariable Long id, @Valid @RequestBody LoanType updated) {
        LoanType existing = loanTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan Type configuration not found with ID: " + id));
        
        existing.setName(updated.getName());
        existing.setInterestRate(updated.getInterestRate());
        existing.setMaxTenureYears(updated.getMaxTenureYears());
        existing.setMaxLoanAmount(updated.getMaxLoanAmount());
        existing.setPenaltyRatePercent(updated.getPenaltyRatePercent());
        existing.setMaxLoansPerCustomerPerLoanType(updated.getMaxLoansPerCustomerPerLoanType());

        return ResponseEntity.ok(loanTypeRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        LoanType existing = loanTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan Type not found with ID: " + id));
        
        loanTypeRepository.delete(existing);
        return ResponseEntity.ok(Map.of("message", "Loan type configurations successfully deleted"));
    }
}