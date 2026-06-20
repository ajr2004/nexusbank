package com.nexusbank.loan_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmiDto(

    Long id,
    BigDecimal amount,
    LocalDate dueDate,
    String status,
    BigDecimal remainingBalance

) {}