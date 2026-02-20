package de.flexis.mycontracts.repository;

import de.flexis.mycontracts.model.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {
}
