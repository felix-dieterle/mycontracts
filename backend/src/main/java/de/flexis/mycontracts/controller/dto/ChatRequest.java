package de.flexis.mycontracts.controller.dto;

import java.util.List;

public record ChatRequest(
    List<ChatMessage> messages,
    Long fileId  // Optional: context for a specific contract file
) {
    public record ChatMessage(
        String role,  // "user" or "assistant"
        String content
    ) {}
}
