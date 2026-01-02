package de.flexis.mycontracts.controller.dto;

public record ChatResponse(
    String message,
    String role,  // "assistant"
    boolean error
) {
    public static ChatResponse success(String message) {
        return new ChatResponse(message, "assistant", false);
    }

    public static ChatResponse error(String message) {
        return new ChatResponse(message, "error", true);
    }
}
