package de.flexis.mycontracts.controller.dto;

import de.flexis.mycontracts.model.StoredFile;
import java.time.Instant;

public record FileListItemResponse(
        Long id,
        String filename,
        String mime,
        Long size,
        String checksum,
        Instant createdAt
) {
    public static FileListItemResponse from(StoredFile f) {
        return new FileListItemResponse(
                f.getId(),
                f.getFilename(),
                f.getMime(),
                f.getSize(),
                f.getChecksum(),
                f.getCreatedAt()
        );
    }
}
