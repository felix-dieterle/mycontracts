package de.flexis.mycontracts.service;

import de.flexis.mycontracts.config.OpenRouterConfig;
import de.flexis.mycontracts.model.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WebSearchService {
    private static final Logger log = LoggerFactory.getLogger(WebSearchService.class);

    private final WebClient webClient;
    private final OpenRouterConfig config;
    private final FileStorageService fileStorageService;

    public WebSearchService(
            WebClient openRouterWebClient,
            OpenRouterConfig config,
            FileStorageService fileStorageService
    ) {
        this.webClient = openRouterWebClient;
        this.config = config;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Perform a web search to find additional information about a contract.
     * This uses AI models that support web search capabilities.
     * 
     * @param fileId The file ID to search information for
     * @param query Optional custom search query. If null, a query will be generated from file metadata
     * @return Search results with additional contract information
     */
    public Map<String, Object> searchContractInfo(Long fileId, String query) {
        if (!config.isConfigured()) {
            return Map.of(
                "success", false,
                "error", "OpenRouter API is not configured. Please set OPENROUTER_API_KEY."
            );
        }

        try {
            // Get the file
            StoredFile file = fileStorageService.get(fileId);

            // Build search query if not provided
            String searchQuery = query;
            if (searchQuery == null || searchQuery.isBlank()) {
                searchQuery = buildSearchQuery(file);
            }

            // Perform web search using AI
            String searchResults = performWebSearch(searchQuery);

            return Map.of(
                "success", true,
                "query", searchQuery,
                "results", searchResults,
                "fileId", fileId,
                "filename", file.getFilename()
            );

        } catch (IllegalArgumentException e) {
            log.error("File not found: {}", fileId, e);
            return Map.of(
                "success", false,
                "error", "File not found: " + fileId
            );
        } catch (Exception e) {
            log.error("Error performing web search for file {}", fileId, e);
            return Map.of(
                "success", false,
                "error", "Error performing web search: " + e.getMessage()
            );
        }
    }

    /**
     * Perform web search and combine with OCR analysis for comprehensive contract information.
     */
    public Map<String, Object> searchAndAnalyze(Long fileId, String searchQuery) {
        if (!config.isConfigured()) {
            return Map.of(
                "success", false,
                "error", "OpenRouter API is not configured. Please set OPENROUTER_API_KEY."
            );
        }

        try {
            StoredFile file = fileStorageService.get(fileId);

            // Build context from file metadata
            String fileContext = buildFileContext(file);

            // Build search query if not provided
            String query = searchQuery;
            if (query == null || query.isBlank()) {
                query = buildSearchQuery(file);
            }

            // Perform comprehensive analysis with web search
            String analysisResults = performComprehensiveAnalysis(fileContext, query);

            return Map.of(
                "success", true,
                "query", query,
                "analysis", analysisResults,
                "fileId", fileId,
                "filename", file.getFilename()
            );

        } catch (Exception e) {
            log.error("Error performing comprehensive analysis for file {}", fileId, e);
            return Map.of(
                "success", false,
                "error", "Error performing analysis: " + e.getMessage()
            );
        }
    }

    private String buildSearchQuery(StoredFile file) {
        StringBuilder query = new StringBuilder();
        
        // Extract potential company/provider name from filename
        String filename = file.getFilename();
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        
        // Replace common separators with spaces
        filename = filename.replaceAll("[_-]", " ");
        
        query.append(filename);

        // Add context from markers if available
        if (file.getMarkersJson() != null && !file.getMarkersJson().isBlank()) {
            query.append(" insurance contract");
        }

        return query.toString().trim();
    }

    private String buildFileContext(StoredFile file) {
        StringBuilder context = new StringBuilder();
        context.append("File: ").append(file.getFilename()).append("\n");
        context.append("Type: ").append(file.getMime()).append("\n");
        
        if (file.getMarkersJson() != null && !file.getMarkersJson().isBlank()) {
            context.append("Markers: ").append(file.getMarkersJson()).append("\n");
        }
        
        if (file.getNote() != null && !file.getNote().isEmpty()) {
            context.append("Note: ").append(file.getNote()).append("\n");
        }
        
        return context.toString();
    }

    private String performWebSearch(String query) {
        // Use AI model with web search capability
        // Note: OpenRouter supports models with web search like perplexity-ai models
        List<Map<String, String>> messages = List.of(
            Map.of(
                "role", "system",
                "content", "You are a helpful assistant that can search the web for information. " +
                        "Provide accurate, up-to-date information from reliable sources."
            ),
            Map.of(
                "role", "user",
                "content", "Search for information about: " + query + "\n\n" +
                        "Please provide detailed information including:\n" +
                        "- Company/provider overview\n" +
                        "- Product details and features\n" +
                        "- Pricing information if available\n" +
                        "- Customer reviews and ratings\n" +
                        "- Any important terms or conditions\n" +
                        "- Recent news or updates"
            )
        );

        try {
            // Try to use a model with web search capabilities (e.g., perplexity)
            // Fallback to regular model if web search model is not available
            String model = getWebSearchModel();
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
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
                    return message.get("content");
                }
            }

            return "No results found";

        } catch (Exception e) {
            log.error("Error performing web search", e);
            return "Error performing web search: " + e.getMessage();
        }
    }

    private String performComprehensiveAnalysis(String fileContext, String searchQuery) {
        List<Map<String, String>> messages = List.of(
            Map.of(
                "role", "system",
                "content", "You are a contract analysis expert with web search capabilities. " +
                        "Provide comprehensive analysis combining file information with web-based research."
            ),
            Map.of(
                "role", "user",
                "content", "Analyze this contract and search for additional information:\n\n" +
                        "Contract Information:\n" + fileContext + "\n\n" +
                        "Search Query: " + searchQuery + "\n\n" +
                        "Please provide:\n" +
                        "1. Provider/company background and reputation\n" +
                        "2. Contract type and features\n" +
                        "3. Market comparison (pricing, terms, benefits)\n" +
                        "4. Potential risks or concerns\n" +
                        "5. Recommendations for optimization\n" +
                        "6. Alternative options if available"
            )
        );

        try {
            String model = getWebSearchModel();
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
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
                    return message.get("content");
                }
            }

            return "No analysis results available";

        } catch (Exception e) {
            log.error("Error performing comprehensive analysis", e);
            return "Error performing analysis: " + e.getMessage();
        }
    }

    private String getWebSearchModel() {
        // Prefer models with web search capabilities
        // Perplexity models have built-in web search
        // Otherwise fallback to configured model
        String configuredModel = config.getModel();
        
        // If user explicitly configured a perplexity model, use it
        if (configuredModel != null && configuredModel.contains("perplexity")) {
            return configuredModel;
        }
        
        // Otherwise use perplexity for web search (it's optimized for this)
        return "perplexity/llama-3.1-sonar-large-128k-online";
    }
}
