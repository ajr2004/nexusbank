package com.nexusbank.loan_service.dto;

import jakarta.validation.constraints.NotNull;

public record LoanStatusUpdateDto(

    @NotNull
    String status,

    String remarks

) {}