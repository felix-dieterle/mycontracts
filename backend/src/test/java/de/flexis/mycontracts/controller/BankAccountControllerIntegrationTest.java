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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BankAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        Path tempDir = Files.createTempDirectory("mycontracts-test-bank");
        r.add("FILE_STORAGE_PATH", () -> tempDir.toString());
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:banktestdb;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @Test
    void createBankAccount() throws Exception {
        mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Main Account\",\"iban\":\"DE89370400440532013000\",\"bankName\":\"Test Bank\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Account"))
                .andExpect(jsonPath("$.iban").value("DE89370400440532013000"))
                .andExpect(jsonPath("$.bankName").value("Test Bank"));
    }

    @Test
    void listBankAccounts() throws Exception {
        mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Savings\",\"bankName\":\"Savings Bank\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/bank-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteBankAccount() throws Exception {
        String result = mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"To Delete\",\"bankName\":\"Old Bank\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractId(result);

        mvc.perform(delete("/api/bank-accounts/" + id))
                .andExpect(status().isOk());

        mvc.perform(get("/api/bank-accounts/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectEmptyName() throws Exception {
        mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"bankName\":\"Some Bank\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAndListTransactions() throws Exception {
        String result = mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Checking\",\"bankName\":\"Check Bank\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long accountId = extractId(result);

        mvc.perform(post("/api/bank-accounts/" + accountId + "/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2024-01-15\",\"amount\":-42.50,\"counterparty\":\"Amazon\",\"description\":\"Order #123\",\"category\":\"Shopping\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(-42.50))
                .andExpect(jsonPath("$.counterparty").value("Amazon"))
                .andExpect(jsonPath("$.category").value("Shopping"));

        mvc.perform(get("/api/bank-accounts/" + accountId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateTransactionCategory() throws Exception {
        String accountResult = mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Budget Account\",\"bankName\":\"Budget Bank\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long accountId = extractId(accountResult);

        String txResult = mvc.perform(post("/api/bank-accounts/" + accountId + "/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2024-02-10\",\"amount\":-15.00,\"counterparty\":\"Spotify\",\"description\":\"Monthly subscription\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long txId = extractId(txResult);

        mvc.perform(patch("/api/bank-accounts/transactions/" + txId + "/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Entertainment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Entertainment"));
    }

    @Test
    void apiKeyNotExposedInResponse() throws Exception {
        String result = mvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"API Account\",\"bankName\":\"API Bank\",\"apiKey\":\"supersecretkey\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert !result.contains("supersecretkey") : "API key should not be returned in response";
    }

    private Long extractId(String json) {
        int idIndex = json.indexOf("\"id\":");
        int comma = json.indexOf(",", idIndex);
        return Long.parseLong(json.substring(idIndex + 5, comma));
    }
}
