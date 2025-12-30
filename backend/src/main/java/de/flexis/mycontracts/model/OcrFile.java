package de.flexis.mycontracts.model;

import de.flexis.mycontracts.model.enums.OcrStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ocr_files")
public class OcrFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String path;

    private String checksum;

    @Lob
    private String rawJson;

    @Enumerated(EnumType.STRING)
    private OcrStatus status = OcrStatus.PENDING;

    @OneToOne
    @JoinColumn(name = "matched_file_id")
    private StoredFile matchedFile;

    private Instant createdAt = Instant.now();
    private Instant processedAt;

    public OcrFile() {}

    public OcrFile(String path, String rawJson) {
        this.path = path;
        this.rawJson = rawJson;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public OcrStatus getStatus() {
        return status;
    }

    public void setStatus(OcrStatus status) {
        this.status = status;
    }

    public StoredFile getMatchedFile() {
        return matchedFile;
    }

    public void setMatchedFile(StoredFile matchedFile) {
        this.matchedFile = matchedFile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
