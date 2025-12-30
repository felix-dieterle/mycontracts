package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.OcrFileRepository;
import de.flexis.mycontracts.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class WatcherServiceIntegrationTest {

    @Autowired
    WatcherService watcherService;

    @Autowired
    StoredFileRepository storedFileRepository;

    @Autowired
    OcrFileRepository ocrFileRepository;

    static Path watchDir;
    static Path storageDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        storageDir = Files.createTempDirectory("mycontracts-storage");
        watchDir = Files.createTempDirectory("mycontracts-watch");
        r.add("FILE_STORAGE_PATH", () -> storageDir.toString());
        r.add("WATCH_DIR", () -> watchDir.toString());
        r.add("watcher.retry-backoff-ms", () -> 0); // allow immediate retry in tests
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
    }

    @Test
    void whenMatchingStoredFile_thenOcrMarkedMatched() throws Exception {
        // create a stored file record with filename "contract1.pdf"
        Path storedFile = storageDir.resolve("contract1.pdf");
        Files.writeString(storedFile, "dummy");
        StoredFile sf = new StoredFile("contract1.pdf", storedFile.toString());
        storedFileRepository.save(sf);

        // create matching ocr json: contract1_ocr.json
        Path ocr = watchDir.resolve("contract1_ocr.json");
        Files.writeString(ocr, "{\"text\":\"sample\"}");

        watcherService.scanOnce();

        List<OcrFile> all = ocrFileRepository.findAll();
        // pick the one that corresponds to our created OCR file
        List<OcrFile> matches = all.stream().filter(o -> o.getPath().endsWith("contract1_ocr.json")).toList();
        assertThat(matches).hasSize(1);
        OcrFile of = matches.get(0);
        assertThat(of.getStatus()).isEqualTo(de.flexis.mycontracts.model.enums.OcrStatus.MATCHED);
        assertThat(of.getMatchedFile()).isNotNull();
        assertThat(of.getMatchedFile().getFilename()).isEqualTo("contract1.pdf");
    }

    @Test
    void whenNoStoredFile_thenOcrPending() throws Exception {
        Path ocr = watchDir.resolve("unknown_ocr.json");
        Files.writeString(ocr, "{\"text\":\"x\"}");

        watcherService.scanOnce();

        List<OcrFile> all = ocrFileRepository.findAll();
        // There may be previous entries; pick the one with path name ending with unknown_ocr.json
        boolean found = all.stream().anyMatch(o -> o.getPath().endsWith("unknown_ocr.json") && o.getStatus().name().equals("PENDING"));
        assertThat(found).isTrue();
    }

    @Test
    void whenPendingThenMatchedOnRetry() throws Exception {
        // create an OCR first without a matching stored file
        Path ocr = watchDir.resolve("willmatch_ocr.json");
        Files.writeString(ocr, "{\"text\":\"x\"}");

        watcherService.scanOnce();

        List<OcrFile> all1 = ocrFileRepository.findAll();
        boolean foundPending = all1.stream().anyMatch(o -> o.getPath().endsWith("willmatch_ocr.json") && o.getStatus().name().equals("PENDING"));
        assertThat(foundPending).isTrue();

        // now add the stored file and retry
        Path storedFile = storageDir.resolve("willmatch.pdf");
        Files.writeString(storedFile, "dummy");
        StoredFile sf = new StoredFile("willmatch.pdf", storedFile.toString());
        storedFileRepository.save(sf);

        watcherService.scanOnce();

        List<OcrFile> all2 = ocrFileRepository.findAll();
        boolean foundMatched = all2.stream().anyMatch(o -> o.getPath().endsWith("willmatch_ocr.json") && o.getStatus().name().equals("MATCHED") && o.getMatchedFile() != null && o.getMatchedFile().getFilename().equals("willmatch.pdf"));
        assertThat(foundMatched).isTrue();
    }
}
