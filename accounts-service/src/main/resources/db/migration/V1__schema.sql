CREATE TABLE customer (
    id   TEXT PRIMARY KEY, -- the Keycloak username, passed on by the gateway as X-User-Id
    name TEXT NOT NULL
);

CREATE TABLE account (
    id          TEXT PRIMARY KEY,
    customer_id TEXT    NOT NULL REFERENCES customer (id),
    type        TEXT    NOT NULL CHECK (type IN ('CURRENT', 'SAVINGS')),
    name        TEXT    NOT NULL,
    currency    CHAR(3) NOT NULL
);

-- No stored balance: an account's balance is the sum of its transactions, so it cannot drift.
CREATE TABLE account_transaction (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id   TEXT        NOT NULL REFERENCES account (id),
    booked_at    TIMESTAMPTZ NOT NULL,
    amount_minor BIGINT      NOT NULL, -- cents; negative means money out
    currency     CHAR(3)     NOT NULL,
    category     TEXT        NOT NULL,
    description  TEXT        NOT NULL,
    counterparty TEXT
);

CREATE INDEX account_transaction_account_booked ON account_transaction (account_id, booked_at);
