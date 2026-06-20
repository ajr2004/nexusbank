package com.nexusbank.loan_service.dto;

import java.math.BigDecimal;

public record LoanResponseDto(

    Long loanId,
    Long userId,
    String accountNumber,

    String loanType,
    BigDecimal amount,
    Double interestRate,
    Integer tenureMonths,

    String status

) {}