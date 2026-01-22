package de.flexis.mycontracts.controller.dto;

import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.enums.OcrStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record FileDetailResponse(
        Long id,
        String filename,
        String mime,
        Long size,
        String checksum,
        List<String> markers,
        Instant dueDate,
        String note,
        Instant createdAt,
        OcrInfo ocr,
        ContractInfo contract
) {
    public static FileDetailResponse from(StoredFile file, OcrFile ocrFile) {
        List<String> markers = file.getMarkersJson() != null && !file.getMarkersJson().isBlank()
                ? Arrays.asList(file.getMarkersJson().split(","))
                : List.of();
        OcrInfo ocr = ocrFile == null ? null : new OcrInfo(
                ocrFile.getId(),
                ocrFile.getStatus(),
                ocrFile.getCreatedAt(),
                ocrFile.getProcessedAt(),
                ocrFile.getRetryCount(),
                ocrFile.getRawJson()
        );
        ContractInfo contract = file.getContract() == null ? null : new ContractInfo(
                file.getContract().getId(),
                file.getContract().getTitle()
        );
        return new FileDetailResponse(
                file.getId(),
                file.getFilename(),
                file.getMime(),
                file.getSize(),
                file.getChecksum(),
                markers,
                file.getDueDate(),
                file.getNote(),
                file.getCreatedAt(),
                ocr,
                contract
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

    public record ContractInfo(
            Long id,
            String title
    ) {}
}
