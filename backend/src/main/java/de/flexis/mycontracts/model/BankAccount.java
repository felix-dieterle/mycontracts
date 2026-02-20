package de.flexis.mycontracts.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String iban;

    private String bankName;

    private String apiProvider;

    @Column
    private String apiKey;

    private boolean active = true;

    private Instant lastSync;

    private Instant createdAt = Instant.now();

    public BankAccount() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getApiProvider() { return apiProvider; }
    public void setApiProvider(String apiProvider) { this.apiProvider = apiProvider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastSync() { return lastSync; }
    public void setLastSync(Instant lastSync) { this.lastSync = lastSync; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
