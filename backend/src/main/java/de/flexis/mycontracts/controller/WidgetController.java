package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.WidgetStatusResponse;
import de.flexis.mycontracts.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Android widget endpoints.
 * Provides savegame metrics and metadata for widget display.
 */
@RestController
@RequestMapping("/api/widget")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WidgetController {

    private final FileStorageService fileStorageService;

    public WidgetController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Get current status snapshot for Android widget.
     * Returns metrics and metadata about the saved state of contracts/files.
     * 
     * @return Widget status with metrics, recent files, and recommendations
     */
    @GetMapping("/status")
    public ResponseEntity<WidgetStatusResponse> getStatus() {
        WidgetStatusResponse status = fileStorageService.getWidgetStatus();
        return ResponseEntity.ok(status);
    }
}
