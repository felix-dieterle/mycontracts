package de.flexis.mycontracts.controller.dto;

public record CreateEmailAccountRequest(
        String name,
        String host,
        Integer port,
        String protocol,
        String username,
        String password
) {}
