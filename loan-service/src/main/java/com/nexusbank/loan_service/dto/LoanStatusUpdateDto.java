package com.nexusbank.loan_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanStatusUpdateDto {

    @NotNull
    private String status; // APPROVED / REJECTED

    private String remarks; // optional
}