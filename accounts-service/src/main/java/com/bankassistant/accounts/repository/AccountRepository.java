package com.bankassistant.accounts.repository;

import java.util.List;
import java.util.Optional;

import com.bankassistant.accounts.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCustomerIdOrderById(String customerId);

    /** Empty when the account does not exist or belongs to another customer. */
    Optional<Account> findByIdAndCustomerId(String id, String customerId);
}
