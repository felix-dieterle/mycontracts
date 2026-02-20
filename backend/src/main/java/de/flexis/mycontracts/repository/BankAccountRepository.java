package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}
