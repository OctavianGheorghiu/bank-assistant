package com.bankassistant.accounts.repository;

import java.util.List;

import com.bankassistant.accounts.model.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Optional filters are built with {@link AccountTransactionSpecifications}. */
public interface AccountTransactionRepository
        extends JpaRepository<AccountTransaction, Long>, JpaSpecificationExecutor<AccountTransaction> {

    List<AccountTransaction> findByAccountId(String accountId);
}
