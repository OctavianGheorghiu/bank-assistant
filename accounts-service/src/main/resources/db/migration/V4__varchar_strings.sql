-- VARCHAR(n) is the standard string type (docs/design/code-conventions.md): no CHAR, no TEXT.
ALTER TABLE customer
    ALTER COLUMN id   TYPE VARCHAR(64),
    ALTER COLUMN name TYPE VARCHAR(100);

ALTER TABLE account
    ALTER COLUMN id          TYPE VARCHAR(64),
    ALTER COLUMN customer_id TYPE VARCHAR(64),
    ALTER COLUMN type        TYPE VARCHAR(16),
    ALTER COLUMN name        TYPE VARCHAR(100),
    ALTER COLUMN currency    TYPE VARCHAR(3);

ALTER TABLE account_transaction
    ALTER COLUMN account_id   TYPE VARCHAR(64),
    ALTER COLUMN currency     TYPE VARCHAR(3),
    ALTER COLUMN category     TYPE VARCHAR(32),
    ALTER COLUMN description  TYPE VARCHAR(255),
    ALTER COLUMN counterparty TYPE VARCHAR(100);
