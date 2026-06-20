package com.nexusbank.banking_service.dto;

import java.math.BigDecimal;

public record WithdrawRequest(

        Long accountId,
        BigDecimal amount

) {}