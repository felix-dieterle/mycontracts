package de.flexis.mycontracts.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BankTransactionResponse(
        Long id,
        Long bankAccountId,
        LocalDate date,
        BigDecimal amount,
        String counterparty,
        String description,
        String category,
        String reference,
        Instant importedAt
) {}
