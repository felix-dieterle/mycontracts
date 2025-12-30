package de.flexis.mycontracts.controller.dto;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.enums.MarkerStatus;
import de.flexis.mycontracts.model.enums.OcrStatus;
import java.time.Instant;

public record FileListItemResponse(
        Long id,
        String filename,
        String mime,
        Long size,
        String checksum,
        Instant createdAt,
        MarkerStatus marker,
        OcrStatus ocrStatus
) {
    public static FileListItemResponse from(StoredFile f, OcrFile ocr) {
        return new FileListItemResponse(
                f.getId(),
                f.getFilename(),
                f.getMime(),
                f.getSize(),
                f.getChecksum(),
                f.getCreatedAt(),
                f.getMarker(),
                ocr != null ? ocr.getStatus() : null
        );
    }
}
