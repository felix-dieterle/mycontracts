package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import de.flexis.mycontracts.controller.dto.FileDetailResponse;
import de.flexis.mycontracts.controller.dto.FileListItemResponse;
import de.flexis.mycontracts.controller.dto.UpdateMarkerRequest;
import de.flexis.mycontracts.controller.dto.UpdateMarkersRequest;
import de.flexis.mycontracts.controller.dto.UpdateDueDateRequest;
import de.flexis.mycontracts.controller.dto.UpdateNoteRequest;
import de.flexis.mycontracts.model.OcrFile;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {

    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<StoredFile> upload(@RequestParam("file") MultipartFile file) throws Exception {
        try {
            StoredFile sf = storageService.store(file);
            return ResponseEntity.ok(sf);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public java.util.List<FileListItemResponse> list() {
        var files = storageService.list();
        var ocrByFile = storageService.findOcrForFileIds(files.stream().map(StoredFile::getId).toList());
        return files.stream()
                .map(f -> FileListItemResponse.from(f, ocrByFile.get(f.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDetailResponse> detail(@PathVariable Long id) {
        try {
            StoredFile file = storageService.get(id);
            var ocr = storageService.findOcrForFile(id).orElse(null);
            return ResponseEntity.ok(FileDetailResponse.from(file, ocr));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/marker")
    public ResponseEntity<StoredFile> updateMarker(@PathVariable Long id, @RequestBody UpdateMarkerRequest request) {
        try {
            StoredFile updated = storageService.updateMarker(id, request.marker());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/markers")
    public ResponseEntity<StoredFile> updateMarkers(@PathVariable Long id, @RequestBody UpdateMarkersRequest request) {
        try {
            StoredFile updated = storageService.updateMarkers(id, request.markers());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/due-date")
    public ResponseEntity<StoredFile> updateDueDate(@PathVariable Long id, @RequestBody UpdateDueDateRequest request) {
        try {
            StoredFile updated = storageService.updateDueDate(id, request.dueDate());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/note")
    public ResponseEntity<StoredFile> updateNote(@PathVariable Long id, @RequestBody UpdateNoteRequest request) {
        try {
            StoredFile updated = storageService.updateNote(id, request.note());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
        Path p = storageService.load(id);
        Resource resource = new UrlResource(p.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
