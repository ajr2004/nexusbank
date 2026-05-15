package com.nexusbank.loan_service.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponseDto {

    private Long loanId;
    private Long userId;
    private Long accountId;

    private String loanType;
    private BigDecimal amount;
    private Double interestRate;
    private Integer tenureMonths;

    private String status;
}