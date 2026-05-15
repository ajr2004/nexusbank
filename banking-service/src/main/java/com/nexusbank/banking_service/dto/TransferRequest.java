package com.nexusbank.banking_service.dto;

import java.math.BigDecimal;

public record TransferRequest(
    String fromAccountNumber,
    String toAccountNumber,
    BigDecimal amount,
    String category
) {}