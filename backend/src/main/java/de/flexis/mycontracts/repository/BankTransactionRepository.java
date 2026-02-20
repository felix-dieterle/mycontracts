package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByBankAccountIdOrderByDateDesc(Long bankAccountId);
}
