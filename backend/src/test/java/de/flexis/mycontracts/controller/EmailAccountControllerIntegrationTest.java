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
public class EmailAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        Path tempDir = Files.createTempDirectory("mycontracts-test-email");
        r.add("FILE_STORAGE_PATH", () -> tempDir.toString());
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:emailtestdb;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @Test
    void createEmailAccount() throws Exception {
        mvc.perform(post("/api/email-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Work Mail\",\"host\":\"imap.example.com\",\"port\":993,\"protocol\":\"IMAP\",\"username\":\"user@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Work Mail"))
                .andExpect(jsonPath("$.host").value("imap.example.com"))
                .andExpect(jsonPath("$.username").value("user@example.com"));
    }

    @Test
    void listEmailAccounts() throws Exception {
        mvc.perform(post("/api/email-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Personal Mail\",\"host\":\"imap.gmail.com\",\"username\":\"personal@gmail.com\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/email-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteEmailAccount() throws Exception {
        String result = mvc.perform(post("/api/email-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Delete Me\",\"host\":\"imap.example.com\",\"username\":\"del@example.com\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractId(result);

        mvc.perform(delete("/api/email-accounts/" + id))
                .andExpect(status().isOk());

        mvc.perform(get("/api/email-accounts/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectEmptyName() throws Exception {
        mvc.perform(post("/api/email-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"host\":\"imap.example.com\",\"username\":\"user@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordNotExposedInResponse() throws Exception {
        String result = mvc.perform(post("/api/email-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Secure Mail\",\"host\":\"imap.example.com\",\"username\":\"user@example.com\",\"password\":\"mysecret\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // password field must not appear in the response body
        assert !result.contains("mysecret") : "Password should not be returned in response";
    }

    private Long extractId(String json) {
        int idIndex = json.indexOf("\"id\":");
        int comma = json.indexOf(",", idIndex);
        return Long.parseLong(json.substring(idIndex + 5, comma));
    }
}
