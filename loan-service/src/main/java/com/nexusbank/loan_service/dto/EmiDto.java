package com.nexusbank.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmiDto {

    private Long id;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;              // ✅ Enum → String
    private BigDecimal remainingBalance; // ✅ Important field
}