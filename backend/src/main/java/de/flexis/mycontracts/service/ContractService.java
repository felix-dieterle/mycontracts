package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.Contract;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.ContractRepository;
import de.flexis.mycontracts.repository.StoredFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final StoredFileRepository storedFileRepository;

    public ContractService(ContractRepository contractRepository, StoredFileRepository storedFileRepository) {
        this.contractRepository = contractRepository;
        this.storedFileRepository = storedFileRepository;
    }

    public List<Contract> listContracts() {
        return contractRepository.findAll();
    }

    public Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
    }

    public Contract createContract(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Contract title cannot be empty");
        }
        Contract contract = new Contract(title);
        return contractRepository.save(contract);
    }

    public Contract updateContract(Long id, String title) {
        Contract contract = getContract(id);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Contract title cannot be empty");
        }
        contract.setTitle(title);
        return contractRepository.save(contract);
    }

    public void deleteContract(Long id) {
        Contract contract = getContract(id);
        // Unlink all files from this contract
        List<StoredFile> linkedFiles = storedFileRepository.findAll().stream()
                .filter(f -> f.getContract() != null && f.getContract().getId().equals(id))
                .toList();
        
        for (StoredFile file : linkedFiles) {
            file.setContract(null);
            storedFileRepository.save(file);
        }
        
        contractRepository.delete(contract);
    }

    public StoredFile linkFileToContract(Long fileId, Long contractId) {
        StoredFile file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        
        if (contractId == null) {
            // Unlink from contract
            file.setContract(null);
        } else {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
            file.setContract(contract);
        }
        
        return storedFileRepository.save(file);
    }

    public List<StoredFile> getFilesForContract(Long contractId) {
        getContract(contractId); // Verify contract exists
        return storedFileRepository.findAll().stream()
                .filter(f -> f.getContract() != null && f.getContract().getId().equals(contractId))
                .toList();
    }
}
