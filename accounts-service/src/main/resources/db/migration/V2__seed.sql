-- Fake demo data. Deterministic: amounts and which days have a purchase come from a hash of
-- (customer, category, days ago), so every fresh database gets the same pattern.
-- shortcut: dates are relative to the day this migration runs, so "last month" always has data,
-- but exact totals depend on when the volume was created; pin a date if evals need fixed answers.

INSERT INTO customer (id, name) VALUES
    ('alice', 'Alice Martin'),
    ('bob',   'Bob Novak'),
    ('carol', 'Carol Rossi');

INSERT INTO account (id, customer_id, type, name, currency)
SELECT c.id || '-' || lower(t.type), c.id, t.type, t.name, 'EUR'
FROM customer c
CROSS JOIN (VALUES ('CURRENT', 'Current account'), ('SAVINGS', 'Savings account')) AS t (type, name);

-- Opening balances, a year ago
INSERT INTO account_transaction (account_id, booked_at, amount_minor, currency, category, description, counterparty)
SELECT a.id,
       current_date - 366,
       CASE a.type WHEN 'CURRENT' THEN 150000 ELSE 500000 END
           + abs(hashtext(a.id || ':opening')) % 100000,
       'EUR', 'opening', 'Opening balance', NULL
FROM account a;

-- Day-to-day spending from the current account
WITH spending (category, every_n_days, min_cents, spread_cents, description, counterparties) AS (
    VALUES ('groceries',      3, 1500, 6000, 'Groceries',         ARRAY['FreshMart', 'Green Grocer', 'Daily Basket']),
           ('restaurants',    5, 1200, 4500, 'Restaurant',        ARRAY['Trattoria Roma', 'Sushi Bar Koi', 'Burger Point']),
           ('transport',      2,  250,  900, 'Public transport',  ARRAY['City Transit']),
           ('shopping',       9, 2000, 9000, 'Online purchase',   ARRAY['ShopNow', 'Book Corner', 'Tech Store']),
           ('entertainment', 12, 1000, 3500, 'Entertainment',     ARRAY['Cinema Lux', 'StreamFlix', 'Concert Hall'])
),
events AS (
    SELECT c.id AS customer_id, s.*, d AS days_ago,
           abs(hashtext(c.id || ':' || s.category || ':' || d)) AS h
    FROM customer c
    CROSS JOIN spending s
    CROSS JOIN generate_series(1, 365) AS d
)
INSERT INTO account_transaction (account_id, booked_at, amount_minor, currency, category, description, counterparty)
SELECT customer_id || '-current',
       (current_date - days_ago) + make_interval(hours => 8 + h % 12, mins => h % 60),
       -(min_cents + (h / 7) % spread_cents),
       'EUR', category, description,
       counterparties[1 + (h / 13) % array_length(counterparties, 1)]
FROM events
WHERE h % every_n_days = 0;

-- Monthly items over the last 12 months: salary, rent, utilities, and a transfer to savings
WITH months AS (
    SELECT (date_trunc('month', current_date) - make_interval(months => m))::date AS month_start
    FROM generate_series(0, 12) AS m
),
monthly (account_type, day_of_month, amount_cents, category, description, counterparty) AS (
    VALUES ('current', 25,  320000, 'salary',    'Monthly salary',       'Acme Software Ltd'),
           ('current',  1, -110000, 'rent',      'Rent',                 'City Apartments'),
           ('current',  5,   -9500, 'utilities', 'Electricity and water', 'Power & Water Co'),
           ('current', 26,  -30000, 'transfer',  'Transfer to savings',  NULL),
           ('savings', 26,   30000, 'transfer',  'Transfer from current', NULL)
)
INSERT INTO account_transaction (account_id, booked_at, amount_minor, currency, category, description, counterparty)
SELECT c.id || '-' || mo.account_type,
       mo_day + time '09:00',
       mo.amount_cents - CASE mo.category WHEN 'utilities' THEN abs(hashtext(c.id || ':' || mo_day)) % 4500 ELSE 0 END,
       'EUR', mo.category, mo.description, mo.counterparty
FROM customer c
CROSS JOIN months m
CROSS JOIN monthly mo
CROSS JOIN LATERAL (SELECT m.month_start + (mo.day_of_month - 1) AS mo_day) AS x
WHERE mo_day > current_date - 366 AND mo_day < current_date;
