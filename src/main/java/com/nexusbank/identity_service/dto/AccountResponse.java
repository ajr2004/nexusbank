package com.nexusbank.identity_service.dto;

import java.math.BigDecimal;

public record AccountResponse(
    String accountNumber,
    BigDecimal balance,
    String accountType
) {}