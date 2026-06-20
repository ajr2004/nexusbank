package com.nexusbank.loan_service.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanRequestDto(
    @NotBlank(message = "Account number is required")
    String accountNumber, // ⚡ Changed from Long accountId

    @NotBlank(message = "Loan type is required")
    String loanType,

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is ₹1000")
    BigDecimal amount,

    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure cannot exceed 30 years")
    Integer tenureMonths
) {}