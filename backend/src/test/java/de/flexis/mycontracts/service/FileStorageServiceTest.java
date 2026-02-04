package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.StoredFileRepository;
import de.flexis.mycontracts.repository.OcrFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private OcrFileRepository ocrFileRepository;

    @Mock
    private MultipartFile multipartFile;

    private StoredFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new StoredFile("test.pdf", "/path/to/test.pdf");
    }

    @Test
    void get_shouldReturnFile_whenFileExists() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));

        // When
        StoredFile result = service.get(1L);

        // Then
        assertNotNull(result);
        assertEquals("test.pdf", result.getFilename());
        verify(storedFileRepository, times(1)).findById(1L);
    }

    @Test
    void get_shouldThrowException_whenFileNotFound() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.get(1L)
        );
        assertEquals("File not found", exception.getMessage());
    }

    @Test
    void list_shouldReturnAllFiles() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        List<StoredFile> files = Arrays.asList(testFile, new StoredFile("test2.pdf", "/path/to/test2.pdf"));
        when(storedFileRepository.findAll()).thenReturn(files);

        // When
        List<StoredFile> result = service.list();

        // Then
        assertEquals(2, result.size());
        verify(storedFileRepository, times(1)).findAll();
    }

    @Test
    void listTasks_shouldReturnFilesWithDueDates() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        List<StoredFile> tasksFiles = Arrays.asList(testFile);
        when(storedFileRepository.findByDueDateNotNullOrderByDueDateAsc()).thenReturn(tasksFiles);

        // When
        List<StoredFile> result = service.listTasks();

        // Then
        assertEquals(1, result.size());
        verify(storedFileRepository, times(1)).findByDueDateNotNullOrderByDueDateAsc();
    }

    @Test
    void updateMarker_shouldUpdateMarker_whenFileExists() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.updateMarker(1L, "URGENT");

        // Then
        assertEquals("URGENT", result.getMarkersJson());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void updateMarker_shouldClearMarker_whenMarkerValueIsNull() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.updateMarker(1L, null);

        // Then
        assertEquals("", result.getMarkersJson());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void updateMarkers_shouldUpdateMultipleMarkers() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);
        List<String> markers = Arrays.asList("URGENT", "REVIEW");

        // When
        StoredFile result = service.updateMarkers(1L, markers);

        // Then
        assertEquals("URGENT,REVIEW", result.getMarkersJson());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void updateMarkers_shouldClearMarkers_whenMarkersListIsEmpty() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.updateMarkers(1L, Arrays.asList());

        // Then
        assertEquals("", result.getMarkersJson());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void updateDueDate_shouldUpdateDueDate_whenFileExists() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        Instant dueDate = Instant.parse("2025-12-31T23:59:59Z");
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.updateDueDate(1L, dueDate);

        // Then
        assertEquals(dueDate, result.getDueDate());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void updateNote_shouldUpdateNote_whenFileExists() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.updateNote(1L, "Test note");

        // Then
        assertEquals("Test note", result.getNote());
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void bulkUpdateMarkers_shouldUpdateMultipleFiles() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        StoredFile file1 = new StoredFile("file1.pdf", "/path/to/file1.pdf");
        StoredFile file2 = new StoredFile("file2.pdf", "/path/to/file2.pdf");
        List<Long> fileIds = Arrays.asList(1L, 2L);
        List<StoredFile> files = Arrays.asList(file1, file2);
        List<String> markers = Arrays.asList("URGENT", "REVIEW");
        
        when(storedFileRepository.findAllById(fileIds)).thenReturn(files);
        when(storedFileRepository.saveAll(files)).thenReturn(files);

        // When
        List<StoredFile> result = service.bulkUpdateMarkers(fileIds, markers);

        // Then
        assertEquals(2, result.size());
        assertEquals("URGENT,REVIEW", file1.getMarkersJson());
        assertEquals("URGENT,REVIEW", file2.getMarkersJson());
        verify(storedFileRepository, times(1)).saveAll(files);
    }

    @Test
    void bulkUpdateDueDate_shouldUpdateMultipleFiles() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        StoredFile file1 = new StoredFile("file1.pdf", "/path/to/file1.pdf");
        StoredFile file2 = new StoredFile("file2.pdf", "/path/to/file2.pdf");
        List<Long> fileIds = Arrays.asList(1L, 2L);
        List<StoredFile> files = Arrays.asList(file1, file2);
        Instant dueDate = Instant.parse("2025-12-31T23:59:59Z");
        
        when(storedFileRepository.findAllById(fileIds)).thenReturn(files);
        when(storedFileRepository.saveAll(files)).thenReturn(files);

        // When
        List<StoredFile> result = service.bulkUpdateDueDate(fileIds, dueDate);

        // Then
        assertEquals(2, result.size());
        assertEquals(dueDate, file1.getDueDate());
        assertEquals(dueDate, file2.getDueDate());
        verify(storedFileRepository, times(1)).saveAll(files);
    }

    @Test
    void bulkUpdateNote_shouldUpdateMultipleFiles() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        StoredFile file1 = new StoredFile("file1.pdf", "/path/to/file1.pdf");
        StoredFile file2 = new StoredFile("file2.pdf", "/path/to/file2.pdf");
        List<Long> fileIds = Arrays.asList(1L, 2L);
        List<StoredFile> files = Arrays.asList(file1, file2);
        String note = "Bulk note";
        
        when(storedFileRepository.findAllById(fileIds)).thenReturn(files);
        when(storedFileRepository.saveAll(files)).thenReturn(files);

        // When
        List<StoredFile> result = service.bulkUpdateNote(fileIds, note);

        // Then
        assertEquals(2, result.size());
        assertEquals(note, file1.getNote());
        assertEquals(note, file2.getNote());
        verify(storedFileRepository, times(1)).saveAll(files);
    }

    @Test
    void save_shouldSaveFile() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.save(testFile)).thenReturn(testFile);

        // When
        StoredFile result = service.save(testFile);

        // Then
        assertNotNull(result);
        verify(storedFileRepository, times(1)).save(testFile);
    }

    @Test
    void delete_shouldDeleteFile_whenFileExists() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.of(testFile));

        // When
        service.delete(1L);

        // Then
        verify(storedFileRepository, times(1)).delete(testFile);
    }

    @Test
    void delete_shouldThrowException_whenFileNotFound() throws Exception {
        // Given
        FileStorageService service = createServiceWithMocks();
        when(storedFileRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.delete(1L)
        );
        assertEquals("File not found", exception.getMessage());
    }

    private FileStorageService createServiceWithMocks() throws Exception {
        return new FileStorageService(
            System.getProperty("java.io.tmpdir") + "/test-storage",
            storedFileRepository,
            ocrFileRepository
        );
    }
}
