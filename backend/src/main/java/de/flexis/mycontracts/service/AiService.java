package de.flexis.mycontracts.service;

import de.flexis.mycontracts.config.OpenRouterConfig;
import de.flexis.mycontracts.controller.dto.ChatRequest;
import de.flexis.mycontracts.controller.dto.ChatResponse;
import de.flexis.mycontracts.controller.dto.ContractOptimizationResponse;
import de.flexis.mycontracts.model.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final WebClient webClient;
    private final OpenRouterConfig config;
    private final FileStorageService fileStorageService;

    public AiService(WebClient openRouterWebClient, OpenRouterConfig config, FileStorageService fileStorageService) {
        this.webClient = openRouterWebClient;
        this.config = config;
        this.fileStorageService = fileStorageService;
    }

    public ChatResponse chat(ChatRequest request) {
        if (!config.isConfigured()) {
            return ChatResponse.error("OpenRouter API is not configured. Please set OPENROUTER_API_KEY.");
        }

        try {
            // Build context if fileId is provided
            String context = "";
            if (request.fileId() != null) {
                try {
                    StoredFile file = fileStorageService.get(request.fileId());
                    context = buildFileContext(file);
                } catch (Exception e) {
                    log.warn("Could not load file context for fileId: {}", request.fileId(), e);
                }
            }

            // Build messages for OpenRouter API
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Add system message with context
            if (!context.isEmpty()) {
                messages.add(Map.of(
                    "role", "system",
                    "content", "You are an AI assistant helping with contract management and optimization. " + context
                ));
            } else {
                messages.add(Map.of(
                    "role", "system",
                    "content", "You are an AI assistant helping with contract management and optimization. " +
                            "You help users understand contracts, identify risks, and suggest improvements."
                ));
            }

            // Add user messages from request
            for (ChatRequest.ChatMessage msg : request.messages()) {
                messages.add(Map.of(
                    "role", msg.role(),
                    "content", msg.content()
                ));
            }

            // Call OpenRouter API
            Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", messages
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    return ChatResponse.success(message.get("content"));
                }
            }

            return ChatResponse.error("No response from AI service");

        } catch (Exception e) {
            log.error("Error calling OpenRouter API", e);
            return ChatResponse.error("Error communicating with AI service: " + e.getMessage());
        }
    }

    public ContractOptimizationResponse optimizeContract(Long fileId) {
        if (!config.isConfigured()) {
            return new ContractOptimizationResponse(
                List.of("OpenRouter API is not configured"),
                List.of(),
                List.of(),
                "Please configure OPENROUTER_API_KEY to use AI optimization features."
            );
        }

        try {
            StoredFile file = fileStorageService.get(fileId);
            String context = buildFileContext(file);

            // Build optimization request
            List<Map<String, String>> messages = List.of(
                Map.of(
                    "role", "system",
                    "content", "You are a contract optimization expert. Analyze contracts and provide specific, " +
                            "actionable suggestions for improvements, identify potential risks, and recommend optimizations."
                ),
                Map.of(
                    "role", "user",
                    "content", "Please analyze this contract and provide: 1) Key suggestions for improvement, " +
                            "2) Potential risks to be aware of, 3) Optimization opportunities. " +
                            "Contract context: " + context + "\n\n" +
                            "Please respond in a structured format with clear sections for suggestions, risks, and improvements."
                )
            );

            Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", messages
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content");
                    
                    return parseOptimizationResponse(content);
                }
            }

            return new ContractOptimizationResponse(
                List.of("Unable to get optimization suggestions"),
                List.of(),
                List.of(),
                "AI service did not return a valid response."
            );

        } catch (IllegalArgumentException e) {
            return new ContractOptimizationResponse(
                List.of("File not found: " + fileId),
                List.of(),
                List.of(),
                "The specified contract file could not be found."
            );
        } catch (Exception e) {
            log.error("Error optimizing contract", e);
            return new ContractOptimizationResponse(
                List.of("Error during optimization: " + e.getMessage()),
                List.of(),
                List.of(),
                "An error occurred while analyzing the contract."
            );
        }
    }

    private String buildFileContext(StoredFile file) {
        StringBuilder context = new StringBuilder();
        context.append("File: ").append(file.getFilename()).append("\n");
        context.append("Type: ").append(file.getMime()).append("\n");
        
        // Parse markers from comma-separated string format (e.g., "URGENT,REVIEW,MISSING_INFO")
        // Note: Despite the field name "markersJson", the actual format is comma-separated, not JSON
        String markersJson = file.getMarkersJson();
        if (markersJson != null && !markersJson.isBlank()) {
            List<String> markers = Arrays.asList(markersJson.split(","));
            if (!markers.isEmpty()) {
                context.append("Markers: ").append(String.join(", ", markers)).append("\n");
            }
        }
        
        if (file.getDueDate() != null) {
            context.append("Due Date: ").append(file.getDueDate()).append("\n");
        }
        
        if (file.getNote() != null && !file.getNote().isEmpty()) {
            context.append("Note: ").append(file.getNote()).append("\n");
        }
        
        return context.toString();
    }

    private ContractOptimizationResponse parseOptimizationResponse(String content) {
        // Simple parsing - in production, this could be more sophisticated
        List<String> suggestions = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> improvements = new ArrayList<>();
        
        String[] lines = content.split("\n");
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("suggestion") || lowerLine.contains("recommend")) {
                currentSection = "suggestions";
            } else if (lowerLine.contains("risk") || lowerLine.contains("concern")) {
                currentSection = "risks";
            } else if (lowerLine.contains("improve") || lowerLine.contains("optimiz")) {
                currentSection = "improvements";
            } else if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*") || 
                       (line.length() > 2 && Character.isDigit(line.charAt(0)) && line.charAt(1) == '.')) {
                String item = line.replaceFirst("^[-•*]\\s*", "").replaceFirst("^\\d+\\.\\s*", "").trim();
                if (!item.isEmpty()) {
                    switch (currentSection) {
                        case "suggestions" -> suggestions.add(item);
                        case "risks" -> risks.add(item);
                        case "improvements" -> improvements.add(item);
                        default -> suggestions.add(item);  // Default to suggestions
                    }
                }
            }
        }
        
        // If parsing didn't yield structured results, treat entire content as summary
        if (suggestions.isEmpty() && risks.isEmpty() && improvements.isEmpty()) {
            suggestions.add("See summary for detailed analysis");
        }
        
        return new ContractOptimizationResponse(
            suggestions.isEmpty() ? List.of("No specific suggestions identified") : suggestions,
            risks.isEmpty() ? List.of("No specific risks identified") : risks,
            improvements.isEmpty() ? List.of("No specific improvements identified") : improvements,
            content
        );
    }
}
