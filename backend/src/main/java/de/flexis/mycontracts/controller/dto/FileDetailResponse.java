package de.flexis.mycontracts.controller.dto;

import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.enums.OcrStatus;

import java.time.Instant;

public record FileDetailResponse(
        Long id,
        String filename,
        String mime,
        Long size,
        String checksum,
        Instant createdAt,
        OcrInfo ocr
) {
    public static FileDetailResponse from(StoredFile file, OcrFile ocrFile) {
        OcrInfo ocr = ocrFile == null ? null : new OcrInfo(
                ocrFile.getId(),
                ocrFile.getStatus(),
                ocrFile.getCreatedAt(),
                ocrFile.getProcessedAt(),
                ocrFile.getRetryCount(),
                ocrFile.getRawJson()
        );
        return new FileDetailResponse(
                file.getId(),
                file.getFilename(),
                file.getMime(),
                file.getSize(),
                file.getChecksum(),
                file.getCreatedAt(),
                ocr
        );
    }

    public record OcrInfo(
            Long id,
            OcrStatus status,
            Instant createdAt,
            Instant processedAt,
            Integer retryCount,
            String rawJson
    ) {}
}
