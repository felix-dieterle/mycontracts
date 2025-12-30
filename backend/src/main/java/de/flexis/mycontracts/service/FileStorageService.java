package de.flexis.mycontracts.service;

import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.repository.StoredFileRepository;
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

    private final Path storagePath;
    private final StoredFileRepository storedFileRepository;

    public FileStorageService(@Value("${FILE_STORAGE_PATH:/data/files}") String storagePath,
                              StoredFileRepository storedFileRepository) throws IOException {
        this.storagePath = Path.of(storagePath);
        this.storedFileRepository = storedFileRepository;
        Files.createDirectories(this.storagePath);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "file";
        Path dest = storagePath.resolve(filename);

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
}
