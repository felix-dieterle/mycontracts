package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.ExtractedField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtractedFieldRepository extends JpaRepository<ExtractedField, Long> {
    List<ExtractedField> findByContractId(Long contractId);
}
