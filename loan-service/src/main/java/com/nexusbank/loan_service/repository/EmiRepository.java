package com.nexusbank.loan_service.repository;

import com.nexusbank.loan_service.model.Emi;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiRepository extends JpaRepository<Emi, Long> {

    // Get EMI schedule
    List<Emi> findByLoanIdOrderByDueDateAsc(Long loanId);

    // Count pending EMIs
    long countByLoanIdAndStatus(Long loanId, Emi.EmiStatus status);

    // Get EMIs by status
    List<Emi> findByStatus(Emi.EmiStatus status);
    List<Emi> findByStatusAndDueDateLessThanEqual(Emi.EmiStatus status, LocalDate date);

    int countByLoanId(Long id);
}