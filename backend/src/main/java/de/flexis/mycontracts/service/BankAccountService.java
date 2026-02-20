package de.flexis.mycontracts.service;

import de.flexis.mycontracts.controller.dto.*;
import de.flexis.mycontracts.model.BankAccount;
import de.flexis.mycontracts.model.BankTransaction;
import de.flexis.mycontracts.repository.BankAccountRepository;
import de.flexis.mycontracts.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                               BankTransactionRepository bankTransactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    public List<BankAccountResponse> listAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BankAccountResponse getAccount(Long id) {
        return toResponse(findById(id));
    }

    public BankAccountResponse createAccount(CreateBankAccountRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        BankAccount account = new BankAccount();
        account.setName(request.name());
        account.setIban(request.iban());
        account.setBankName(request.bankName());
        account.setApiProvider(request.apiProvider());
        account.setApiKey(request.apiKey());
        return toResponse(bankAccountRepository.save(account));
    }

    public BankAccountResponse updateAccount(Long id, CreateBankAccountRequest request) {
        BankAccount account = findById(id);
        if (request.name() != null && !request.name().isBlank()) {
            account.setName(request.name());
        }
        if (request.iban() != null) {
            account.setIban(request.iban());
        }
        if (request.bankName() != null) {
            account.setBankName(request.bankName());
        }
        if (request.apiProvider() != null) {
            account.setApiProvider(request.apiProvider());
        }
        if (request.apiKey() != null) {
            account.setApiKey(request.apiKey());
        }
        return toResponse(bankAccountRepository.save(account));
    }

    public void deleteAccount(Long id) {
        BankAccount account = findById(id);
        bankTransactionRepository.deleteAll(
                bankTransactionRepository.findByBankAccountIdOrderByDateDesc(id));
        bankAccountRepository.delete(account);
    }

    public List<BankTransactionResponse> listTransactions(Long bankAccountId) {
        findById(bankAccountId); // verify account exists
        return bankTransactionRepository.findByBankAccountIdOrderByDateDesc(bankAccountId).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public BankTransactionResponse addTransaction(Long bankAccountId, CreateBankTransactionRequest request) {
        BankAccount account = findById(bankAccountId);
        BankTransaction tx = new BankTransaction();
        tx.setBankAccount(account);
        tx.setDate(request.date());
        tx.setAmount(request.amount());
        tx.setCounterparty(request.counterparty());
        tx.setDescription(request.description());
        tx.setCategory(request.category());
        tx.setReference(request.reference());
        return toTransactionResponse(bankTransactionRepository.save(tx));
    }

    public BankTransactionResponse updateTransactionCategory(Long transactionId,
                                                              UpdateTransactionCategoryRequest request) {
        BankTransaction tx = bankTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        tx.setCategory(request.category());
        return toTransactionResponse(bankTransactionRepository.save(tx));
    }

    public void deleteTransaction(Long transactionId) {
        BankTransaction tx = bankTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        bankTransactionRepository.delete(tx);
    }

    private BankAccount findById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return new BankAccountResponse(
                account.getId(),
                account.getName(),
                account.getIban(),
                account.getBankName(),
                account.getApiProvider(),
                account.isActive(),
                account.getLastSync(),
                account.getCreatedAt()
        );
    }

    private BankTransactionResponse toTransactionResponse(BankTransaction tx) {
        return new BankTransactionResponse(
                tx.getId(),
                tx.getBankAccount() != null ? tx.getBankAccount().getId() : null,
                tx.getDate(),
                tx.getAmount(),
                tx.getCounterparty(),
                tx.getDescription(),
                tx.getCategory(),
                tx.getReference(),
                tx.getImportedAt()
        );
    }
}
