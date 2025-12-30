package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.Contract;
import de.flexis.mycontracts.model.ExtractedField;
import de.flexis.mycontracts.model.OcrFile;
import de.flexis.mycontracts.model.StoredFile;
import de.flexis.mycontracts.model.enums.FieldSource;
import de.flexis.mycontracts.model.enums.OcrStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RepositoryIntegrationTest {

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private OcrFileRepository ocrFileRepository;

    @Autowired
    private ExtractedFieldRepository extractedFieldRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    void saveAndLoadFileAndOcrAndField() {
        StoredFile f = new StoredFile("vertrag.pdf", "/data/incoming/vertrag.pdf");
        f.setMime("application/pdf");
        f.setChecksum("abc123");
        storedFileRepository.save(f);

        OcrFile o = new OcrFile("/data/incoming/vertrag_ocr.json", "{\"text\": \"Dies ist ein Test\"}");
        o.setChecksum("ocr123");
        o.setStatus(OcrStatus.PENDING);
        o.setMatchedFile(f);
        ocrFileRepository.save(o);

        Contract c = new Contract("Test Contract");
        c = contractRepository.save(c);

        ExtractedField ef = new ExtractedField(c, "amount", "100.00 EUR", 0.9, FieldSource.RULE);
        extractedFieldRepository.save(ef);

        assertThat(storedFileRepository.findByFilename("vertrag.pdf")).isNotNull();
        assertThat(ocrFileRepository.findById(o.getId())).isPresent();
        assertThat(extractedFieldRepository.findByContractId(c.getId())).isNotEmpty();
    }
}
