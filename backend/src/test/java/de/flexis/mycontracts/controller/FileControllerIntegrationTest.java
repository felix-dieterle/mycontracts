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

    @Test
    void bulkUpdateMarkers() throws Exception {
        // Upload test files
        MockMultipartFile file1 = new MockMultipartFile("file", "bulk1.txt", MediaType.TEXT_PLAIN_VALUE, "bulk1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "bulk2.txt", MediaType.TEXT_PLAIN_VALUE, "bulk2".getBytes());

        String result1 = mvc.perform(multipart("/api/files/upload").file(file1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String result2 = mvc.perform(multipart("/api/files/upload").file(file2))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id1 = extractIdFromJson(result1);
        Long id2 = extractIdFromJson(result2);

        // Bulk update markers
        mvc.perform(patch("/api/files/bulk/markers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[" + id1 + "," + id2 + "],\"markers\":[\"URGENT\",\"REVIEW\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].markersJson").value("URGENT,REVIEW"))
                .andExpect(jsonPath("$[1].markersJson").value("URGENT,REVIEW"));
    }

    @Test
    void bulkUpdateDueDate() throws Exception {
        // Upload test files
        MockMultipartFile file1 = new MockMultipartFile("file", "bulkdue1.txt", MediaType.TEXT_PLAIN_VALUE, "bulkdue1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "bulkdue2.txt", MediaType.TEXT_PLAIN_VALUE, "bulkdue2".getBytes());

        String result1 = mvc.perform(multipart("/api/files/upload").file(file1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String result2 = mvc.perform(multipart("/api/files/upload").file(file2))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id1 = extractIdFromJson(result1);
        Long id2 = extractIdFromJson(result2);

        // Bulk update due date
        mvc.perform(patch("/api/files/bulk/due-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[" + id1 + "," + id2 + "],\"dueDate\":\"2026-12-31T23:59:59Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dueDate").value("2026-12-31T23:59:59Z"))
                .andExpect(jsonPath("$[1].dueDate").value("2026-12-31T23:59:59Z"));
    }

    @Test
    void updateNote() throws Exception {
        // Upload test file
        MockMultipartFile file = new MockMultipartFile("file", "note-test.txt", MediaType.TEXT_PLAIN_VALUE, "note test".getBytes());

        String result = mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractIdFromJson(result);

        // Update note
        mvc.perform(patch("/api/files/" + id + "/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":\"This is a test note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("This is a test note"));
    }

    @Test
    void updateMarkers() throws Exception {
        // Upload test file
        MockMultipartFile file = new MockMultipartFile("file", "markers-test.txt", MediaType.TEXT_PLAIN_VALUE, "markers test".getBytes());

        String result = mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = extractIdFromJson(result);

        // Update markers
        mvc.perform(patch("/api/files/" + id + "/markers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"markers\":[\"URGENT\",\"MISSING_INFO\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markersJson").value("URGENT,MISSING_INFO"));
    }

    @Test
    void getFile_returnsNotFound_whenFileDoesNotExist() throws Exception {
        mvc.perform(get("/api/files/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mvc.perform(multipart("/api/files/upload").file(emptyFile))
                .andExpect(status().isBadRequest());
    }
}
