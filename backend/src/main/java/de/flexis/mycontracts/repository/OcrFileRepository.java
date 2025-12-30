package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.OcrFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrFileRepository extends JpaRepository<OcrFile, Long> {
    List<OcrFile> findByStatus(String status);
}
