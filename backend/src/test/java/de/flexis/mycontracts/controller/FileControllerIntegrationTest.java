package de.flexis.mycontracts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    static Path tempDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        tempDir = Files.createTempDirectory("mycontracts-test-files");
        r.add("FILE_STORAGE_PATH", () -> tempDir.toString());
        // Ensure an in-memory JDBC DB is available for full context startup
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        // Override driver and credentials so the in-memory DB works even if application.properties
        // specifies a different JDBC driver (e.g., SQLite) for normal runs.
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @Test
    void uploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk());

        // assert file exists in storage
        Path stored = tempDir.resolve("test.txt");
        assert Files.exists(stored);
    }

    @Test
    void rejectPathTraversal() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "../evil.txt", MediaType.TEXT_PLAIN_VALUE, "boom".getBytes());

        mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isBadRequest());

        Path outside = tempDir.resolve("../evil.txt");
        Path sanitized = tempDir.resolve("evil.txt");
        assert Files.notExists(outside.normalize());
        assert Files.notExists(sanitized);
    }

    @Test
    void rejectOversizedFile() throws Exception {
        byte[] bigPayload = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile("file", "big.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, bigPayload);

        mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isBadRequest());

        Path stored = tempDir.resolve("big.bin");
        assert Files.notExists(stored);
    }

    @Test
    void listTasksReturnsSortedByDueDate() throws Exception {
        // Upload files
        MockMultipartFile file1 = new MockMultipartFile("file", "task1.txt", MediaType.TEXT_PLAIN_VALUE, "task1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "task2.txt", MediaType.TEXT_PLAIN_VALUE, "task2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", "task3.txt", MediaType.TEXT_PLAIN_VALUE, "task3".getBytes());

        String result1 = mvc.perform(multipart("/api/files/upload").file(file1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String result2 = mvc.perform(multipart("/api/files/upload").file(file2))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String result3 = mvc.perform(multipart("/api/files/upload").file(file3))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract IDs from JSON responses
        Long id1 = extractIdFromJson(result1);
        Long id2 = extractIdFromJson(result2);
        Long id3 = extractIdFromJson(result3);

        // Set due dates: file2 (earliest), file1 (middle), file3 (latest)
        mvc.perform(patch("/api/files/" + id1 + "/due-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dueDate\":\"2026-02-15T00:00:00Z\"}"))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/files/" + id2 + "/due-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dueDate\":\"2026-01-30T00:00:00Z\"}"))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/files/" + id3 + "/due-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dueDate\":\"2026-03-10T00:00:00Z\"}"))
                .andExpect(status().isOk());

        // Get tasks list and verify ordering
        mvc.perform(get("/api/files/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id2))
                .andExpect(jsonPath("$[1].id").value(id1))
                .andExpect(jsonPath("$[2].id").value(id3));
    }

    private Long extractIdFromJson(String json) {
        // Simple extraction assuming format: {"id":123,...}
        int idIndex = json.indexOf("\"id\":");
        int comma = json.indexOf(",", idIndex);
        String idStr = json.substring(idIndex + 5, comma);
        return Long.parseLong(idStr);
    }
}
