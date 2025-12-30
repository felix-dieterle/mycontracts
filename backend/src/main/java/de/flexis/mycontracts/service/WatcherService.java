package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.enums.OcrStatus;
import de.flexis.mycontracts.repository.OcrFileRepository;
import de.flexis.mycontracts.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatcherService {

    private final Path watchDir;
    private final StoredFileRepository storedFileRepository;
    private final OcrFileRepository ocrFileRepository;
    private final io.micrometer.core.instrument.Counter matchedCounter;
    private final io.micrometer.core.instrument.Counter pendingCounter;
    private final io.micrometer.core.instrument.Counter failedCounter;
    private final io.micrometer.core.instrument.Counter retryCounter;

    // configuration
    @org.springframework.beans.factory.annotation.Value("${watcher.max-retries:5}")
    private int maxRetries;

    @org.springframework.beans.factory.annotation.Value("${watcher.retry-backoff-ms:5000}")
    private long retryBackoffMs;

    private boolean enabled = true;

    public WatcherService(@Value("${WATCH_DIR:/data/incoming}") String watchDir,
                         StoredFileRepository storedFileRepository,
                         OcrFileRepository ocrFileRepository,
                         ObjectProvider<MeterRegistry> meterRegistryProvider) throws IOException {
        this.watchDir = Path.of(watchDir);
        this.storedFileRepository = storedFileRepository;
        this.ocrFileRepository = ocrFileRepository;
        MeterRegistry registry = meterRegistryProvider.getIfAvailable(SimpleMeterRegistry::new);
        this.matchedCounter = registry.counter("watcher.ocr.matched");
        this.pendingCounter = registry.counter("watcher.ocr.pending");
        this.failedCounter = registry.counter("watcher.ocr.failed");
        this.retryCounter = registry.counter("watcher.ocr.retry");
        try {
            Files.createDirectories(this.watchDir);
        } catch (IOException e) {
            // If we cannot create/watch the directory (e.g., permission denied), disable the watcher
            // so the application can still start and tests that don't use the watcher won't fail.
            this.enabled = false;
            org.slf4j.LoggerFactory.getLogger(WatcherService.class)
                    .warn("Watcher disabled, cannot access WATCH_DIR {}: {}", this.watchDir, e.toString());
            return;
        }
    }

    // scheduled scan runs every 5s by default
    @Scheduled(fixedDelayString = "${watcher.scan-interval-ms:5000}")
    public void scheduledScan() {
        if (!enabled) return;
        try {
            scanOnce();
        } catch (Exception e) {
            // avoid crashing scheduler
            org.slf4j.LoggerFactory.getLogger(WatcherService.class).error("Watcher scan failed", e);
        }
    }

    // public for tests
    public List<OcrFile> scanOnce() throws IOException {
        if (!enabled) return List.of();
        List<Path> ocrFiles = Files.list(watchDir)
                .filter(p -> p.getFileName().toString().endsWith("_ocr.json"))
                .collect(Collectors.toList());

        for (Path p : ocrFiles) {
            String pathStr = p.toAbsolutePath().toString();
            // skip if already processed and present in DB
            if (ocrFileRepository.findByPath(pathStr).isPresent()) continue;

            String rawJson = Files.readString(p);
            OcrFile of = new OcrFile(pathStr, rawJson);
            of.setChecksum(computeChecksum(p));

            // determine basename (strip suffix _ocr.json)
            String filename = p.getFileName().toString();
            String base = filename.substring(0, filename.length() - "_ocr.json".length());

            // try to find matching stored file (by filename base without extension)
            StoredFile matched = findMatchingStoredFile(base);
            if (matched != null) {
                of.setMatchedFile(matched);
                of.setStatus(OcrStatus.MATCHED);
                of.setProcessedAt(Instant.now());
                matchedCounter.increment();
            } else {
                of.setStatus(OcrStatus.PENDING);
                pendingCounter.increment();
            }

            ocrFileRepository.save(of);
        }

        // attempt to rematch pending OCRs on each scan
        processPending();

        return ocrFileRepository.findAll();
    }

    private void processPending() {
        List<de.flexis.mycontracts.model.OcrFile> pending = ocrFileRepository.findByStatus(de.flexis.mycontracts.model.enums.OcrStatus.PENDING);
        for (var of : pending) {
            try {
                Instant now = Instant.now();
                if (of.getLastAttempt() != null && now.isBefore(of.getLastAttempt().plusMillis(retryBackoffMs))) {
                    continue; // respect backoff window
                }

                // extract filename from path
                Path p = Path.of(of.getPath());
                String filename = p.getFileName().toString();
                String base = filename;
                if (base.endsWith("_ocr.json")) base = base.substring(0, base.length() - "_ocr.json".length());

                StoredFile matched = findMatchingStoredFile(base);
                of.setLastAttempt(now);
                of.setRetryCount(of.getRetryCount() + 1);
                retryCounter.increment();
                if (matched != null) {
                    of.setMatchedFile(matched);
                    of.setStatus(de.flexis.mycontracts.model.enums.OcrStatus.MATCHED);
                    of.setProcessedAt(Instant.now());
                    matchedCounter.increment();
                } else if (of.getRetryCount() >= maxRetries) {
                    of.setStatus(de.flexis.mycontracts.model.enums.OcrStatus.FAILED);
                    of.setProcessedAt(Instant.now());
                    failedCounter.increment();
                }
                ocrFileRepository.save(of);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(WatcherService.class).warn("Failed to process pending OCR {}", of.getPath(), e);
            }
        }
    }
    private StoredFile findMatchingStoredFile(String base) {
        List<StoredFile> all = storedFileRepository.findAll();
        for (StoredFile sf : all) {
            String fname = sf.getFilename();
            String fnameBase = stripExtension(fname);
            if (fnameBase.equals(base)) return sf;
        }
        return null;
    }

    private String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx > 0) return filename.substring(0, idx);
        return filename;
    }

    private String computeChecksum(Path file) throws IOException {
        try (var in = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) md.update(buf, 0, r);
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
