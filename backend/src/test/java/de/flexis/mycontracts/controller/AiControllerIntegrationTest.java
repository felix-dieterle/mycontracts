package de.flexis.mycontracts.controller;

import de.flexis.mycontracts.controller.dto.ChatRequest;
import de.flexis.mycontracts.controller.dto.ChatResponse;
import de.flexis.mycontracts.controller.dto.ContractOptimizationRequest;
import de.flexis.mycontracts.controller.dto.ContractOptimizationResponse;
import de.flexis.mycontracts.controller.dto.OcrAnalysisRequest;
import de.flexis.mycontracts.controller.dto.WebSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testChatEndpoint_withoutApiKey_returnsError() {
        // Given
        ChatRequest.ChatMessage message = new ChatRequest.ChatMessage("user", "Hello");
        ChatRequest request = new ChatRequest(List.of(message), null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                "/api/ai/chat",
                entity,
                ChatResponse.class
        );

        // Then
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isTrue();
        assertThat(response.getBody().message()).contains("not configured");
    }

    @Test
    void testOptimizeEndpoint_withoutApiKey_returnsError() {
        // Given
        ContractOptimizationRequest request = new ContractOptimizationRequest(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ContractOptimizationRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<ContractOptimizationResponse> response = restTemplate.postForEntity(
                "/api/ai/optimize",
                entity,
                ContractOptimizationResponse.class
        );

        // Then - should return OK but with error message in response
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().summary()).contains("OPENROUTER_API_KEY");
    }

    @Test
    void testChatEndpoint_withContextFileId() {
        // Given
        ChatRequest.ChatMessage message = new ChatRequest.ChatMessage("user", "What is this contract about?");
        ChatRequest request = new ChatRequest(List.of(message), 1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                "/api/ai/chat",
                entity,
                ChatResponse.class
        );

        // Then - should fail gracefully when API key is not configured
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isTrue();
    }

    @Test
    void testAnalyzeOcrEndpoint_withoutApiKey_returnsError() {
        // Given
        OcrAnalysisRequest request = new OcrAnalysisRequest(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OcrAnalysisRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/ai/analyze-ocr",
                entity,
                Map.class
        );

        // Then - should return error when API key is not configured
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error").toString()).contains("not configured");
    }

    @Test
    void testWebSearchEndpoint_withoutApiKey_returnsError() {
        // Given
        WebSearchRequest request = new WebSearchRequest(1L, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebSearchRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/ai/web-search",
                entity,
                Map.class
        );

        // Then - should return error when API key is not configured
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error").toString()).contains("not configured");
    }

    @Test
    void testSearchAndAnalyzeEndpoint_withoutApiKey_returnsError() {
        // Given
        WebSearchRequest request = new WebSearchRequest(1L, "test query");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebSearchRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/ai/search-and-analyze",
                entity,
                Map.class
        );

        // Then - should return error when API key is not configured
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error").toString()).contains("not configured");
    }
}
