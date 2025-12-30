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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
