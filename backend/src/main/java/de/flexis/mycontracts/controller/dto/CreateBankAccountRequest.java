package de.flexis.mycontracts.controller.dto;

public record CreateBankAccountRequest(
        String name,
        String iban,
        String bankName,
        String apiProvider,
        String apiKey
) {}
