package de.flexis.mycontracts.model;

import jakarta.persistence.*;
import java.time.Instant;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.Set;
import java.util.HashSet;

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

    @Lob
    private String markersJson; // JSON array: ["URGENT","REVIEW",...]

    private Instant dueDate;

    @Lob
    private String note;

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

    public String getMarkersJson() {
        return markersJson;
    }

    public void setMarkersJson(String markersJson) {
        this.markersJson = markersJson;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
