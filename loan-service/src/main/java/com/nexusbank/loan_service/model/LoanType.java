package com.nexusbank.loan_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanTypeId;

    @NotBlank(message = "Loan type name is required")
    @Size(max = 100, message = "Loan type name cannot exceed 100 characters")
    private String name;

    @DecimalMin(value = "6.5", inclusive = true, message = "Interest rate must be at least 6.5%")
    @DecimalMax(value = "15.0", message = "Interest rate cannot exceed 15 percent")
    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Builder.Default
    @Positive(message = "Max loans per customer must be greater than 0")
    @Max(value = 3, message = "Max loans per customer cannot exceed 3")
    private int maxLoansPerCustomerPerLoanType = 3;

    @Min(value = 1, message = "Maximum tenure years must be at least 1")
    @Max(value = 30, message = "Maximum tenure years cannot exceed 30")
    private int maxTenureYears;

    @DecimalMin(value = "20000.00", inclusive = true, message = "Maximum loan amount must be at least ₹20,000")
    @Column(precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    @DecimalMin(value = "0.0", message = "Penalty rate cannot be negative")
    @DecimalMax(value = "5.0", message = "Penalty rate cannot exceed 5 percent")
    @Column(precision = 5, scale = 2)
    private BigDecimal penaltyRatePercent;
}