package de.flexis.mycontracts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flexis.mycontracts.controller.dto.WidgetStatusResponse;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WidgetControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StoredFileRepository fileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static Path tempDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        tempDir = Files.createTempDirectory("mycontracts-test-widget");
        r.add("FILE_STORAGE_PATH", () -> tempDir.toString());
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:widgetdb;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @BeforeEach
    void setUp() {
        fileRepository.deleteAll();
    }

    @Test
    void getWidgetStatus_emptyDatabase() throws Exception {
        MvcResult result = mvc.perform(get("/api/widget/status"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        WidgetStatusResponse response = objectMapper.readValue(json, WidgetStatusResponse.class);

        assertThat(response.totalFiles()).isEqualTo(0);
        assertThat(response.needsAttention()).isEqualTo(0);
        assertThat(response.recentFiles()).isEmpty();
        assertThat(response.recommendations()).isNotEmpty();
        assertThat(response.recommendations().get(0)).contains("Alle kritischen Punkte sind bearbeitet");
    }

    @Test
    void getWidgetStatus_withFiles() throws Exception {
        // Create a file with urgent marker and overdue date
        StoredFile file1 = new StoredFile("urgent.pdf", tempDir.resolve("urgent.pdf").toString());
        file1.setMarkersJson("URGENT,REVIEW");
        file1.setDueDate(Instant.now().minusSeconds(86400)); // 1 day ago (overdue)
        file1.setSize(1000L);
        file1.setChecksum("abc123");
        fileRepository.save(file1);

        // Create a file with missing info
        StoredFile file2 = new StoredFile("missing.pdf", tempDir.resolve("missing.pdf").toString());
        file2.setMarkersJson("MISSING_INFO");
        file2.setDueDate(Instant.now().plusSeconds(86400 * 20)); // 20 days from now
        file2.setSize(2000L);
        file2.setChecksum("def456");
        fileRepository.save(file2);

        // Create a file that needs categorization (no markers, no due date, no note)
        StoredFile file3 = new StoredFile("uncategorized.pdf", tempDir.resolve("uncategorized.pdf").toString());
        file3.setSize(3000L);
        file3.setChecksum("ghi789");
        fileRepository.save(file3);

        MvcResult result = mvc.perform(get("/api/widget/status"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        WidgetStatusResponse response = objectMapper.readValue(json, WidgetStatusResponse.class);

        assertThat(response.totalFiles()).isEqualTo(3);
        assertThat(response.needsAttention()).isEqualTo(2); // file1 (urgent + overdue) and file2 (missing_info)
        assertThat(response.overdueCount()).isEqualTo(1); // file1
        assertThat(response.urgentCount()).isEqualTo(1); // file1
        assertThat(response.upcomingDueDates30Days()).isEqualTo(2); // file1 and file2
        assertThat(response.missingInfo()).isEqualTo(1); // file2
        assertThat(response.needsCategorization()).isEqualTo(1); // file3
        assertThat(response.recentFiles()).hasSize(3);
        assertThat(response.recommendations()).hasSizeGreaterThan(0);
    }

    @Test
    void getWidgetStatus_recentFilesLimit() throws Exception {
        // Create 10 files
        for (int i = 0; i < 10; i++) {
            StoredFile file = new StoredFile("file" + i + ".pdf", tempDir.resolve("file" + i + ".pdf").toString());
            file.setSize(1000L);
            file.setChecksum("checksum" + i);
            fileRepository.save(file);
            Thread.sleep(10); // Ensure different creation times
        }

        MvcResult result = mvc.perform(get("/api/widget/status"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        WidgetStatusResponse response = objectMapper.readValue(json, WidgetStatusResponse.class);

        assertThat(response.totalFiles()).isEqualTo(10);
        assertThat(response.recentFiles()).hasSize(5); // Limited to 5 most recent
    }
}
