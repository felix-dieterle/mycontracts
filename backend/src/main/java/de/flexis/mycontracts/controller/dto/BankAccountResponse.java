package de.flexis.mycontracts.controller.dto;

import java.time.Instant;

public record BankAccountResponse(
        Long id,
        String name,
        String iban,
        String bankName,
        String apiProvider,
        boolean active,
        Instant lastSync,
        Instant createdAt
) {}
