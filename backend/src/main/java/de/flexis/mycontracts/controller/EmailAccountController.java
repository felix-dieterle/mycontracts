package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.CreateEmailAccountRequest;
import de.flexis.mycontracts.controller.dto.EmailAccountResponse;
import de.flexis.mycontracts.service.EmailAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    public EmailAccountController(EmailAccountService emailAccountService) {
        this.emailAccountService = emailAccountService;
    }

    @GetMapping
    public List<EmailAccountResponse> list() {
        return emailAccountService.listAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailAccountResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(emailAccountService.getAccount(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EmailAccountResponse> create(@RequestBody CreateEmailAccountRequest request) {
        try {
            return ResponseEntity.ok(emailAccountService.createAccount(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailAccountResponse> update(@PathVariable Long id,
                                                        @RequestBody CreateEmailAccountRequest request) {
        try {
            return ResponseEntity.ok(emailAccountService.updateAccount(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            emailAccountService.deleteAccount(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
