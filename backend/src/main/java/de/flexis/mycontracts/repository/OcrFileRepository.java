package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.enums.OcrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrFileRepository extends JpaRepository<OcrFile, Long> {
    List<OcrFile> findByStatus(OcrStatus status);
    java.util.Optional<OcrFile> findByPath(String path);
    java.util.Optional<OcrFile> findByMatchedFileId(Long matchedFileId);
}
