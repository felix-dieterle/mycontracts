package de.flexis.mycontracts.controller.dto;

public record ChatResponse(
    String message,
    String role,  // "assistant"
    boolean error,
    RateLimitInfo rateLimit
) {
    public static ChatResponse success(String message) {
        return new ChatResponse(message, "assistant", false, null);
    }

    public static ChatResponse success(String message, RateLimitInfo rateLimit) {
        return new ChatResponse(message, "assistant", false, rateLimit);
    }

    public static ChatResponse error(String message) {
        return new ChatResponse(message, "error", true, null);
    }
}
