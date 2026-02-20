package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.*;
import de.flexis.mycontracts.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    public List<BankAccountResponse> list() {
        return bankAccountService.listAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bankAccountService.getAccount(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<BankAccountResponse> create(@RequestBody CreateBankAccountRequest request) {
        try {
            return ResponseEntity.ok(bankAccountService.createAccount(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BankAccountResponse> update(@PathVariable Long id,
                                                       @RequestBody CreateBankAccountRequest request) {
        try {
            return ResponseEntity.ok(bankAccountService.updateAccount(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            bankAccountService.deleteAccount(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<BankTransactionResponse>> listTransactions(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bankAccountService.listTransactions(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<BankTransactionResponse> addTransaction(
            @PathVariable Long id,
            @RequestBody CreateBankTransactionRequest request) {
        try {
            return ResponseEntity.ok(bankAccountService.addTransaction(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/transactions/{transactionId}/category")
    public ResponseEntity<BankTransactionResponse> updateCategory(
            @PathVariable Long transactionId,
            @RequestBody UpdateTransactionCategoryRequest request) {
        try {
            return ResponseEntity.ok(bankAccountService.updateTransactionCategory(transactionId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/transactions/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        try {
            bankAccountService.deleteTransaction(transactionId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
