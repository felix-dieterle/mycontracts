package de.flexis.mycontracts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class ContractControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    static Path tempDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        tempDir = Files.createTempDirectory("mycontracts-test-contracts");
        r.add("FILE_STORAGE_PATH", () -> tempDir.toString());
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @Test
    void createContract() throws Exception {
        mvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Contract\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Contract"));
    }

    @Test
    void listContracts() throws Exception {
        // Create a contract first
        mvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Contract 1\"}"))
                .andExpect(status().isOk());

        // List contracts
        mvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateContract() throws Exception {
        // Create a contract
        String result = mvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Original Title\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractIdFromJson(result);

        // Update it
        mvc.perform(put("/api/contracts/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteContract() throws Exception {
        // Create a contract
        String result = mvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"To Delete\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractIdFromJson(result);

        // Delete it
        mvc.perform(delete("/api/contracts/" + id))
                .andExpect(status().isOk());

        // Verify it's gone
        mvc.perform(get("/api/contracts/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectEmptyTitle() throws Exception {
        mvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private Long extractIdFromJson(String json) {
        int idIndex = json.indexOf("\"id\":");
        int comma = json.indexOf(",", idIndex);
        String idStr = json.substring(idIndex + 5, comma);
        return Long.parseLong(idStr);
    }
}
