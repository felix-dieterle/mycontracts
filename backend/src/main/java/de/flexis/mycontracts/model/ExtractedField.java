package de.flexis.mycontracts.model;

import de.flexis.mycontracts.model.enums.FieldSource;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "extracted_fields")
public class ExtractedField {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(nullable = false)
    private String fieldName;

    @Lob
    @Column(name = "field_value")
    private String fieldValue;

    private Double confidence;

    @Enumerated(EnumType.STRING)
    private FieldSource source;

    private Instant createdAt = Instant.now();

    public ExtractedField() {}

    public ExtractedField(Contract contract, String fieldName, String fieldValue, Double confidence, FieldSource source) {
        this.contract = contract;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.confidence = confidence;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public FieldSource getSource() {
        return source;
    }

    public void setSource(FieldSource source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
