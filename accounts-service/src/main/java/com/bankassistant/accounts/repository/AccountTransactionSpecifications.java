package com.bankassistant.accounts.repository;

import java.time.OffsetDateTime;
import java.util.Locale;

import com.bankassistant.accounts.model.entity.AccountTransaction;
import org.springframework.data.jpa.domain.Specification;

/** Building blocks for transaction searches, combined with {@code and} only for the filters a caller sets. */
public final class AccountTransactionSpecifications {

    private AccountTransactionSpecifications() {
    }

    public static Specification<AccountTransaction> ofAccount(String accountId) {
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }

    public static Specification<AccountTransaction> bookedAtOrAfter(OffsetDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("bookedAt"), from);
    }

    public static Specification<AccountTransaction> bookedBefore(OffsetDateTime before) {
        return (root, query, cb) -> cb.lessThan(root.get("bookedAt"), before);
    }

    /** Ignores case, so "Groceries" finds the "groceries" rows. */
    public static Specification<AccountTransaction> inCategory(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase(Locale.ROOT));
    }
}
