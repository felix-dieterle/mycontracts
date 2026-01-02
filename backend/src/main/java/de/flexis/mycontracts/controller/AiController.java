package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.ChatRequest;
import de.flexis.mycontracts.controller.dto.ChatResponse;
import de.flexis.mycontracts.controller.dto.ContractOptimizationRequest;
import de.flexis.mycontracts.controller.dto.ContractOptimizationResponse;
import de.flexis.mycontracts.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
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
}
