-- Store the balance on the account (decision #37). The transactions stay as the ledger:
-- any code that inserts a transaction must update balance_minor in the same database transaction.
ALTER TABLE account ADD COLUMN balance_minor BIGINT NOT NULL DEFAULT 0;

UPDATE account a
SET balance_minor = coalesce((SELECT sum(t.amount_minor) FROM account_transaction t WHERE t.account_id = a.id), 0);

ALTER TABLE account ALTER COLUMN balance_minor DROP DEFAULT;
