package de.flexis.mycontracts.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "files")
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;

    private String mime;
    private Long size;
    private String checksum;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private Instant createdAt = Instant.now();

    public StoredFile() {}

    public StoredFile(String filename, String path) {
        this.filename = filename;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
