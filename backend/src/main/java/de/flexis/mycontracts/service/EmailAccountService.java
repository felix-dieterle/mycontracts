package de.flexis.mycontracts.service;

import de.flexis.mycontracts.controller.dto.CreateEmailAccountRequest;
import de.flexis.mycontracts.controller.dto.EmailAccountResponse;
import de.flexis.mycontracts.model.EmailAccount;
import de.flexis.mycontracts.repository.EmailAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;

    public EmailAccountService(EmailAccountRepository emailAccountRepository) {
        this.emailAccountRepository = emailAccountRepository;
    }

    public List<EmailAccountResponse> listAccounts() {
        return emailAccountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public EmailAccountResponse getAccount(Long id) {
        return toResponse(findById(id));
    }

    public EmailAccountResponse createAccount(CreateEmailAccountRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (request.host() == null || request.host().isBlank()) {
            throw new IllegalArgumentException("Host cannot be empty");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        EmailAccount account = new EmailAccount();
        account.setName(request.name());
        account.setHost(request.host());
        account.setPort(request.port() != null ? request.port() : 993);
        account.setProtocol(request.protocol() != null ? request.protocol() : "IMAP");
        account.setUsername(request.username());
        account.setPassword(request.password());
        return toResponse(emailAccountRepository.save(account));
    }

    public EmailAccountResponse updateAccount(Long id, CreateEmailAccountRequest request) {
        EmailAccount account = findById(id);
        if (request.name() != null && !request.name().isBlank()) {
            account.setName(request.name());
        }
        if (request.host() != null && !request.host().isBlank()) {
            account.setHost(request.host());
        }
        if (request.port() != null) {
            account.setPort(request.port());
        }
        if (request.protocol() != null) {
            account.setProtocol(request.protocol());
        }
        if (request.username() != null && !request.username().isBlank()) {
            account.setUsername(request.username());
        }
        if (request.password() != null) {
            account.setPassword(request.password());
        }
        return toResponse(emailAccountRepository.save(account));
    }

    public void deleteAccount(Long id) {
        emailAccountRepository.delete(findById(id));
    }

    private EmailAccount findById(Long id) {
        return emailAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email account not found"));
    }

    private EmailAccountResponse toResponse(EmailAccount account) {
        return new EmailAccountResponse(
                account.getId(),
                account.getName(),
                account.getHost(),
                account.getPort(),
                account.getProtocol(),
                account.getUsername(),
                account.isActive(),
                account.getLastSync(),
                account.getCreatedAt()
        );
    }
}
