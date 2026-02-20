package de.flexis.mycontracts.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBankTransactionRequest(
        LocalDate date,
        BigDecimal amount,
        String counterparty,
        String description,
        String category,
        String reference
) {}
