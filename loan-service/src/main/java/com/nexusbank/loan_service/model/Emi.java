package com.nexusbank.loan_service.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;

    private LocalDate paymentDate;

    private BigDecimal remainingBalance;

    public enum EmiStatus {
        PENDING,
        PAID,
        LATE
    }
}