package com.nexusbank.banking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(

    Long transactionId,
    BigDecimal amount,
    String type,
    String category,
    String description,
    String targetAccountNumber,
    LocalDateTime timestamp

) {}