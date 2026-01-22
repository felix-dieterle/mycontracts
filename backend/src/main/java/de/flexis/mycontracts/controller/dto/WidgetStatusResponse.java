package de.flexis.mycontracts.controller.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for Android widget showing savegame metrics and metadata.
 * Provides a snapshot of the current state of contracts and files.
 */
public record WidgetStatusResponse(
        // Timestamp of this snapshot
        Instant timestamp,
        
        // Total counts
        int totalFiles,
        
        // Files requiring immediate attention
        int needsAttention,
        int overdueCount,
        int urgentCount,
        
        // Due dates
        int upcomingDueDates30Days,
        
        // OCR status
        int ocrPending,
        int ocrFailed,
        int ocrMatched,
        
        // Categorization
        int missingInfo,
        int needsCategorization,
        
        // Recent activity
        List<RecentFile> recentFiles,
        
        // Recommendations summary
        List<String> recommendations
) {
    public record RecentFile(
            Long id,
            String filename,
            Instant createdAt,
            List<String> markers,
            String ocrStatus,
            Instant dueDate
    ) {}
}
