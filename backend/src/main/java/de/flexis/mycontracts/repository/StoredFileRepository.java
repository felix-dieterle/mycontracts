package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    StoredFile findByFilename(String filename);
    List<StoredFile> findByDueDateNotNullOrderByDueDateAsc();
    List<StoredFile> findByContractId(Long contractId);
}
