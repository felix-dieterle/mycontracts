package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.Contract;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.ContractRepository;
import de.flexis.mycontracts.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private StoredFileRepository storedFileRepository;

    @InjectMocks
    private ContractService contractService;

    private Contract testContract;
    private StoredFile testFile;

    @BeforeEach
    void setUp() {
        testContract = new Contract("Test Contract");
        testFile = new StoredFile("test.pdf", "/path/to/test.pdf");
    }

    @Test
    void listContracts_shouldReturnAllContracts() {
        // Given
        List<Contract> contracts = Arrays.asList(testContract, new Contract("Another Contract"));
        when(contractRepository.findAll()).thenReturn(contracts);

        // When
        List<Contract> result = contractService.listContracts();

        // Then
        assertEquals(2, result.size());
        verify(contractRepository, times(1)).findAll();
    }

    @Test
    void getContract_shouldReturnContract_whenContractExists() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // When
        Contract result = contractService.getContract(1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Contract", result.getTitle());
        verify(contractRepository, times(1)).findById(1L);
    }

    @Test
    void getContract_shouldThrowException_whenContractNotFound() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.getContract(1L)
        );
        assertEquals("Contract not found", exception.getMessage());
    }

    @Test
    void createContract_shouldCreateContract_whenTitleIsValid() {
        // Given
        String title = "New Contract";
        Contract newContract = new Contract(title);
        when(contractRepository.save(any(Contract.class))).thenReturn(newContract);

        // When
        Contract result = contractService.createContract(title);

        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void createContract_shouldThrowException_whenTitleIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.createContract(null)
        );
        assertEquals("Contract title cannot be empty", exception.getMessage());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void createContract_shouldThrowException_whenTitleIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.createContract("   ")
        );
        assertEquals("Contract title cannot be empty", exception.getMessage());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void updateContract_shouldUpdateContract_whenTitleIsValid() {
        // Given
        String newTitle = "Updated Contract";
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(testContract)).thenReturn(testContract);

        // When
        Contract result = contractService.updateContract(1L, newTitle);

        // Then
        assertEquals(newTitle, result.getTitle());
        verify(contractRepository, times(1)).save(testContract);
    }

    @Test
    void updateContract_shouldThrowException_whenTitleIsNull() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.updateContract(1L, null)
        );
        assertEquals("Contract title cannot be empty", exception.getMessage());
    }

    @Test
    void updateContract_shouldThrowException_whenContractNotFound() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.updateContract(1L, "New Title")
        );
        assertEquals("Contract not found", exception.getMessage());
    }

    @Test
    void deleteContract_shouldDeleteContract_andUnlinkFiles() {
        // Given
        List<StoredFile> linkedFiles = Arrays.asList(testFile, new StoredFile("test2.pdf", "/path/to/test2.pdf"));
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(storedFileRepository.findByContractId(1L)).thenReturn(linkedFiles);

        // When
        contractService.deleteContract(1L);

        // Then
        verify(storedFileRepository, times(2)).save(any(StoredFile.class));
        verify(contractRepository, times(1)).delete(testContract);
    }

    @Test
    void deleteContract_shouldThrowException_whenContractNotFound() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.deleteContract(1L)
        );
        assertEquals("Contract not found", exception.getMessage());
    }

    @Test
    void linkFileToContract_shouldLinkFile_whenBothExist() {
        // Given
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = contractService.linkFileToContract(1L, 1L);

        // Then
        assertEquals(testContract, result.getContract());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void linkFileToContract_shouldUnlinkFile_whenContractIdIsNull() {
        // Given
        testFile.setContract(testContract);
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = contractService.linkFileToContract(1L, null);

        // Then
        assertNull(result.getContract());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void linkFileToContract_shouldThrowException_whenFileNotFound() {
        // Given
        when(storedFileRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.linkFileToContract(1L, 1L)
        );
        assertEquals("File not found", exception.getMessage());
    }

    @Test
    void linkFileToContract_shouldThrowException_whenContractNotFound() {
        // Given
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.linkFileToContract(1L, 1L)
        );
        assertEquals("Contract not found", exception.getMessage());
    }

    @Test
    void getFilesForContract_shouldReturnFiles_whenContractExists() {
        // Given
        List<StoredFile> files = Arrays.asList(testFile, new StoredFile("test2.pdf", "/path/to/test2.pdf"));
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(storedFileRepository.findByContractId(1L)).thenReturn(files);

        // When
        List<StoredFile> result = contractService.getFilesForContract(1L);

        // Then
        assertEquals(2, result.size());
        verify(storedFileRepository, times(1)).findByContractId(1L);
    }

    @Test
    void getFilesForContract_shouldThrowException_whenContractNotFound() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> contractService.getFilesForContract(1L)
        );
        assertEquals("Contract not found", exception.getMessage());
    }
}
