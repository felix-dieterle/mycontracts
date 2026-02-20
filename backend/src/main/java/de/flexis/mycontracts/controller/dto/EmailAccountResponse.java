package de.flexis.mycontracts.controller.dto;

import java.time.Instant;

public record EmailAccountResponse(
        Long id,
        String name,
        String host,
        int port,
        String protocol,
        String username,
        boolean active,
        Instant lastSync,
        Instant createdAt
) {}
