package de.flexis.mycontracts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flexis.mycontracts.config.OpenRouterConfig;
import de.flexis.mycontracts.model.Contract;
import de.flexis.mycontracts.model.ExtractedField;
import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.enums.FieldSource;
import de.flexis.mycontracts.repository.ContractRepository;
import de.flexis.mycontracts.repository.ExtractedFieldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OcrAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(OcrAnalysisService.class);

    private final WebClient webClient;
    private final OpenRouterConfig config;
    private final FileStorageService fileStorageService;
    private final ContractRepository contractRepository;
    private final ExtractedFieldRepository extractedFieldRepository;
    private final ObjectMapper objectMapper;

    public OcrAnalysisService(
            WebClient openRouterWebClient,
            OpenRouterConfig config,
            FileStorageService fileStorageService,
            ContractRepository contractRepository,
            ExtractedFieldRepository extractedFieldRepository,
            ObjectMapper objectMapper
    ) {
        this.webClient = openRouterWebClient;
        this.config = config;
        this.fileStorageService = fileStorageService;
        this.contractRepository = contractRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> analyzeOcrData(Long fileId) {
        if (!config.isConfigured()) {
            return Map.of(
                "success", false,
                "error", "OpenRouter API is not configured. Please set OPENROUTER_API_KEY."
            );
        }

        try {
            // Get the file and its OCR data
            StoredFile file = fileStorageService.get(fileId);
            Optional<OcrFile> ocrFileOpt = fileStorageService.findOcrForFile(fileId);
            
            if (ocrFileOpt.isEmpty()) {
                return Map.of(
                    "success", false,
                    "error", "No OCR data found for file ID: " + fileId
                );
            }

            OcrFile ocrFile = ocrFileOpt.get();
            String ocrJson = ocrFile.getRawJson();

            if (ocrJson == null || ocrJson.isBlank()) {
                return Map.of(
                    "success", false,
                    "error", "OCR data is empty for file ID: " + fileId
                );
            }

            // Extract fields using AI
            Map<String, String> extractedData = extractFieldsWithAI(file.getFilename(), ocrJson);

            // Create or get contract
            Contract contract = getOrCreateContract(file);

            // Save extracted fields
            List<ExtractedField> savedFields = new ArrayList<>();
            for (Map.Entry<String, String> entry : extractedData.entrySet()) {
                ExtractedField field = new ExtractedField(
                    contract,
                    entry.getKey(),
                    entry.getValue(),
                    0.8, // Default confidence for AI extraction
                    FieldSource.LLM
                );
                savedFields.add(extractedFieldRepository.save(field));
            }

            // Link the file to the contract if not already linked
            if (file.getContract() == null) {
                file.setContract(contract);
                fileStorageService.save(file);
            }

            return Map.of(
                "success", true,
                "contractId", contract.getId(),
                "extractedFields", extractedData,
                "fieldCount", savedFields.size()
            );

        } catch (IllegalArgumentException e) {
            log.error("File not found: {}", fileId, e);
            return Map.of(
                "success", false,
                "error", "File not found: " + fileId
            );
        } catch (Exception e) {
            log.error("Error analyzing OCR data for file {}", fileId, e);
            return Map.of(
                "success", false,
                "error", "Error analyzing OCR data: " + e.getMessage()
            );
        }
    }

    private Map<String, String> extractFieldsWithAI(String filename, String ocrJson) {
        String prompt = buildExtractionPrompt(filename, ocrJson);

        List<Map<String, String>> messages = List.of(
            Map.of(
                "role", "system",
                "content", "You are a contract analysis expert. Extract structured information from OCR data. " +
                        "Respond ONLY with valid JSON containing the extracted fields. Do not include any markdown formatting or explanation."
            ),
            Map.of(
                "role", "user",
                "content", prompt
            )
        );

        Map<String, Object> requestBody = Map.of(
            "model", config.getModel(),
            "messages", messages,
            "temperature", 0.3 // Lower temperature for more consistent extraction
        );

        try {
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
                    
                    // Parse the JSON response
                    return parseExtractedFields(content);
                }
            }

            return Map.of();

        } catch (Exception e) {
            log.error("Error calling AI for field extraction", e);
            return Map.of();
        }
    }

    private String buildExtractionPrompt(String filename, String ocrJson) {
        return String.format(
            "Analyze the following OCR data from the contract file '%s' and extract these standard contract fields " +
            "(especially for insurance contracts). Return ONLY a JSON object with the extracted values. " +
            "If a field is not found, omit it from the response.\n\n" +
            "Expected fields:\n" +
            "- description: Brief summary of what this contract is about\n" +
            "- cost_per_month: Monthly cost/premium (with currency if available)\n" +
            "- cost_per_year: Annual cost/premium (with currency if available)\n" +
            "- return_on_death: Death benefit or return on death\n" +
            "- return_on_quitting: Surrender value or return on quitting\n" +
            "- payment_hold_option: Whether payments can be set on hold (yes/no)\n" +
            "- current_value: Current cash value or policy value\n" +
            "- contract_type: Type of contract (e.g., life insurance, health insurance, etc.)\n" +
            "- provider: Insurance provider or company name\n" +
            "- contract_number: Policy or contract number\n" +
            "- start_date: Contract start date\n" +
            "- end_date: Contract end date or maturity date\n" +
            "- cancellation_period: Notice period for cancellation\n" +
            "- coverage_amount: Total coverage or insured amount\n\n" +
            "OCR Data:\n%s",
            filename,
            ocrJson
        );
    }

    private Map<String, String> parseExtractedFields(String content) {
        try {
            // Clean up the content - remove markdown code blocks if present
            String cleanedContent = content.trim();
            if (cleanedContent.startsWith("```json")) {
                cleanedContent = cleanedContent.substring(7);
            } else if (cleanedContent.startsWith("```")) {
                cleanedContent = cleanedContent.substring(3);
            }
            if (cleanedContent.endsWith("```")) {
                cleanedContent = cleanedContent.substring(0, cleanedContent.length() - 3);
            }
            cleanedContent = cleanedContent.trim();

            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(cleanedContent);
            Map<String, String> result = new java.util.HashMap<>();
            
            jsonNode.fields().forEachRemaining(entry -> {
                String value = entry.getValue().asText();
                if (value != null && !value.isBlank()) {
                    result.put(entry.getKey(), value);
                }
            });

            return result;

        } catch (Exception e) {
            log.error("Error parsing extracted fields from AI response: {}", content, e);
            return Map.of();
        }
    }

    private Contract getOrCreateContract(StoredFile file) {
        if (file.getContract() != null) {
            return file.getContract();
        }

        // Create a new contract based on the filename
        String contractTitle = file.getFilename();
        if (contractTitle.contains(".")) {
            contractTitle = contractTitle.substring(0, contractTitle.lastIndexOf('.'));
        }

        Contract contract = new Contract(contractTitle);
        return contractRepository.save(contract);
    }
}
