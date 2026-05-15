package com.nexusbank.loan_service.repository;

import com.nexusbank.loan_service.model.Loan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Get all loans of a user
    List<Loan> findByUserId(Long userId);

    // Count loans by user + status
    long countByUserIdAndStatus(Long userId, Loan.LoanStatus status);

    // Get loans by status
    List<Loan> findByStatus(Loan.LoanStatus status);

    // Count by status (for admin dashboard later)
    long countByStatus(Loan.LoanStatus status);

    // Active loans (PENDING + APPROVED)
    List<Loan> findByUserIdAndStatusIn(Long userId, List<Loan.LoanStatus> statuses);
}
