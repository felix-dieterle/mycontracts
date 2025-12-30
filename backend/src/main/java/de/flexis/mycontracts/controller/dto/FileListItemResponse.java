package de.flexis.mycontracts.controller.dto;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.enums.OcrStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record FileListItemResponse(
        Long id,
        String filename,
        String mime,
        Long size,
        String checksum,
        Instant createdAt,
        List<String> markers,
        OcrStatus ocrStatus,
        Instant dueDate
) {
    public static FileListItemResponse from(StoredFile f, OcrFile ocr) {
        List<String> markers = f.getMarkersJson() != null && !f.getMarkersJson().isBlank()
                ? Arrays.asList(f.getMarkersJson().split(","))
                : List.of();
        return new FileListItemResponse(
                f.getId(),
                f.getFilename(),
                f.getMime(),
                f.getSize(),
                f.getChecksum(),
                f.getCreatedAt(),
                markers,
                ocr != null ? ocr.getStatus() : null,
                f.getDueDate()
        );
    }
}
