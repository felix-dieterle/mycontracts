package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.ChatRequest;
import de.flexis.mycontracts.controller.dto.ChatResponse;
import de.flexis.mycontracts.controller.dto.ContractOptimizationRequest;
import de.flexis.mycontracts.controller.dto.ContractOptimizationResponse;
import de.flexis.mycontracts.controller.dto.OcrAnalysisRequest;
import de.flexis.mycontracts.controller.dto.WebSearchRequest;
import de.flexis.mycontracts.service.AiService;
import de.flexis.mycontracts.service.OcrAnalysisService;
import de.flexis.mycontracts.service.WebSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiController {

    private final AiService aiService;
    private final OcrAnalysisService ocrAnalysisService;
    private final WebSearchService webSearchService;

    public AiController(AiService aiService, OcrAnalysisService ocrAnalysisService, WebSearchService webSearchService) {
        this.aiService = aiService;
        this.ocrAnalysisService = ocrAnalysisService;
        this.webSearchService = webSearchService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        if (response.error()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/optimize")
    public ResponseEntity<ContractOptimizationResponse> optimize(@RequestBody ContractOptimizationRequest request) {
        ContractOptimizationResponse response = aiService.optimizeContract(request.fileId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-ocr")
    public ResponseEntity<Map<String, Object>> analyzeOcr(@RequestBody OcrAnalysisRequest request) {
        Map<String, Object> response = ocrAnalysisService.analyzeOcrData(request.fileId());
        boolean success = (boolean) response.getOrDefault("success", false);
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/web-search")
    public ResponseEntity<Map<String, Object>> webSearch(@RequestBody WebSearchRequest request) {
        Map<String, Object> response = webSearchService.searchContractInfo(request.fileId(), request.query());
        boolean success = (boolean) response.getOrDefault("success", false);
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search-and-analyze")
    public ResponseEntity<Map<String, Object>> searchAndAnalyze(@RequestBody WebSearchRequest request) {
        Map<String, Object> response = webSearchService.searchAndAnalyze(request.fileId(), request.query());
        boolean success = (boolean) response.getOrDefault("success", false);
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
