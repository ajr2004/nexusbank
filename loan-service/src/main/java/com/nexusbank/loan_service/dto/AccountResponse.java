package com.nexusbank.loan_service.dto;

import java.math.BigDecimal;

public record AccountResponse(
    Long accountId,
    String accountNumber,
    String accountType,
    BigDecimal balance
) {}
