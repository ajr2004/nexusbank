package com.nexusbank.loan_service.repository;

import com.nexusbank.loan_service.model.Emi;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiRepository extends JpaRepository<Emi, Long> {

    // Get EMI schedule
    List<Emi> findByLoanIdOrderByDueDateAsc(Long loanId);

    // Count pending EMIs
    long countByLoanIdAndStatus(Long loanId, Emi.EmiStatus status);

    // Get EMIs by status
    List<Emi> findByStatus(Emi.EmiStatus status);

    int countByLoanId(Long id);
}