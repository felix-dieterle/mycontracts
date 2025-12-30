package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.StoredFileRepository;
import de.flexis.mycontracts.repository.OcrFileRepository;
import de.flexis.mycontracts.model.OcrFile;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class FileStorageService {

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB hard limit

    private final Path storagePath;
    private final StoredFileRepository storedFileRepository;
    private final OcrFileRepository ocrFileRepository;

    public FileStorageService(@Value("${FILE_STORAGE_PATH:/data/files}") String storagePath,
                              StoredFileRepository storedFileRepository,
                              OcrFileRepository ocrFileRepository) throws IOException {
        this.storagePath = Path.of(storagePath);
        this.storedFileRepository = storedFileRepository;
        this.ocrFileRepository = ocrFileRepository;
        Files.createDirectories(this.storagePath);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        String filename = sanitizeFilename(file.getOriginalFilename());

        long size = file.getSize();
        if (size <= 0) {
            throw new IllegalArgumentException("File is empty");
        }
        if (size > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File too large");
        }

        Path dest = storagePath.resolve(filename).normalize();
        if (!dest.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        // copy stream to destination atomically
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        StoredFile sf = new StoredFile(filename, dest.toAbsolutePath().toString());
        sf.setMime(file.getContentType());
        sf.setSize(file.getSize());
        sf.setChecksum(checksum(dest));
        return storedFileRepository.save(sf);
    }

    public Path load(Long id) {
        return storedFileRepository.findById(id)
                .map(sf -> Path.of(sf.getPath()))
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public StoredFile get(Long id) {
        return storedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public Optional<OcrFile> findOcrForFile(Long fileId) {
        return ocrFileRepository.findByMatchedFileId(fileId);
    }

    private String checksum(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) md.update(buf, 0, r);
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeFilename(String original) {
        String filename = original != null ? original.trim() : "file";
        if (filename.isEmpty()) filename = "file";

        // Normalize separators and drop any path components
        filename = filename.replace('\\', '/');
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        filename = Path.of(filename).getFileName().toString();

        return filename;
    }
}
