package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.CreateContractRequest;
import de.flexis.mycontracts.controller.dto.LinkFileToContractRequest;
import de.flexis.mycontracts.model.Contract;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<Contract> list() {
        return contractService.listContracts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> get(@PathVariable Long id) {
        try {
            Contract contract = contractService.getContract(id);
            return ResponseEntity.ok(contract);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Contract> create(@RequestBody CreateContractRequest request) {
        try {
            Contract contract = contractService.createContract(request.title());
            return ResponseEntity.ok(contract);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> update(@PathVariable Long id, @RequestBody CreateContractRequest request) {
        try {
            Contract contract = contractService.updateContract(id, request.title());
            return ResponseEntity.ok(contract);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<StoredFile>> getFiles(@PathVariable Long id) {
        try {
            List<StoredFile> files = contractService.getFilesForContract(id);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/files/{fileId}/link")
    public ResponseEntity<StoredFile> linkFile(@PathVariable Long fileId, @RequestBody LinkFileToContractRequest request) {
        try {
            StoredFile file = contractService.linkFileToContract(fileId, request.contractId());
            return ResponseEntity.ok(file);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
