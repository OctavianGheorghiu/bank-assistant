package com.bankassistant.accounts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Accounts, balances and transactions of the calling customer. The caller is identified by the
 * X-User-Id header, which only the gateway sets; an account of another customer answers 404.
 */
@RestController
@RequestMapping("/accounts")
class AccountsController {

    static final String USER_HEADER = "X-User-Id";

    record AccountView(String id, String name, String type, String currency, String balance) {}

    record Balance(String accountId, String amount, String currency) {}

    record Transaction(long id, OffsetDateTime bookedAt, String amount, String currency,
                       String category, String description, String counterparty) {}

    private final JdbcClient db;

    AccountsController(JdbcClient db) {
        this.db = db;
    }

    @GetMapping
    List<AccountView> accounts(@RequestHeader(USER_HEADER) String userId) {
        return db.sql("""
                SELECT a.id, a.name, a.type, a.currency, coalesce(sum(t.amount_minor), 0) AS balance
                FROM account a LEFT JOIN account_transaction t ON t.account_id = a.id
                WHERE a.customer_id = :user
                GROUP BY a.id ORDER BY a.id""")
                .param("user", userId)
                .query((rs, i) -> new AccountView(rs.getString("id"), rs.getString("name"), rs.getString("type"),
                        rs.getString("currency"), decimal(rs.getLong("balance"))))
                .list();
    }

    @GetMapping("/{accountId}/balance")
    Balance balance(@RequestHeader(USER_HEADER) String userId, @PathVariable String accountId) {
        String currency = ownedAccountCurrency(userId, accountId);
        long minor = db.sql("SELECT coalesce(sum(amount_minor), 0) FROM account_transaction WHERE account_id = :id")
                .param("id", accountId)
                .query(Long.class).single();
        return new Balance(accountId, decimal(minor), currency);
    }

    /** Newest first; from and to are inclusive dates, all filters optional. */
    @GetMapping("/{accountId}/transactions")
    List<Transaction> transactions(@RequestHeader(USER_HEADER) String userId, @PathVariable String accountId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @RequestParam(required = false) String category) {
        ownedAccountCurrency(userId, accountId);
        return db.sql("""
                SELECT id, booked_at, amount_minor, currency, category, description, counterparty
                FROM account_transaction
                WHERE account_id = :id
                  AND (CAST(:from AS date) IS NULL OR booked_at >= CAST(:from AS date))
                  AND (CAST(:to AS date) IS NULL OR booked_at < CAST(:to AS date) + 1)
                  AND (CAST(:category AS text) IS NULL OR category = :category)
                ORDER BY booked_at DESC, id DESC""")
                .param("id", accountId)
                .param("from", from)
                .param("to", to)
                .param("category", category)
                .query((rs, i) -> new Transaction(rs.getLong("id"), rs.getObject("booked_at", OffsetDateTime.class),
                        decimal(rs.getLong("amount_minor")), rs.getString("currency"), rs.getString("category"),
                        rs.getString("description"), rs.getString("counterparty")))
                .list();
    }

    private String ownedAccountCurrency(String userId, String accountId) {
        return db.sql("SELECT currency FROM account WHERE id = :id AND customer_id = :user")
                .param("id", accountId)
                .param("user", userId)
                .query(String.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    // shortcut: assumes 2 decimal places (EUR only); look up the currency's digits if other currencies appear
    private static String decimal(long minor) {
        return BigDecimal.valueOf(minor, 2).toPlainString();
    }
}
