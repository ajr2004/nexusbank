package com.nexusbank.banking_service.dto;

import java.math.BigDecimal;

public record DepositRequest(
    Long accountId,
    BigDecimal amount
) {}
