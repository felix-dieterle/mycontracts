package de.flexis.mycontracts.controller.dto;

public record RateLimitInfo(
    Integer limit,
    Integer remaining,
    Long resetAt,
    String apiName
) {
    public static RateLimitInfo empty(String apiName) {
        return new RateLimitInfo(null, null, null, apiName);
    }

    public static RateLimitInfo of(Integer limit, Integer remaining, Long resetAt, String apiName) {
        return new RateLimitInfo(limit, remaining, resetAt, apiName);
    }

    /**
     * Calculate usage percentage (0-100)
     */
    public Integer getUsagePercentage() {
        if (limit == null || remaining == null || limit == 0) {
            return null;
        }
        int used = Math.max(0, limit - remaining);
        return (int) Math.round((double) used / limit * 100);
    }

    /**
     * Get status color based on usage percentage
     * - green: 0-69%
     * - yellow: 70-89%
     * - red: 90-100%
     */
    public String getStatusColor() {
        Integer usage = getUsagePercentage();
        if (usage == null) {
            return "gray";
        }
        if (usage < 70) {
            return "green";
        } else if (usage < 90) {
            return "yellow";
        } else {
            return "red";
        }
    }
}
