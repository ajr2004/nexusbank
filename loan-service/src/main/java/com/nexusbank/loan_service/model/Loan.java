package com.nexusbank.loan_service.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference only (NO User object)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long accountId; // from banking service

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private String loanType;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;

    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CLOSED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = LoanStatus.PENDING;
    }
}