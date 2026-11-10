#!/bin/sh
# Runs once, on the first start with an empty data volume.
# shortcut: a schema added in a later phase needs `docker compose down -v` (wipes data) or running its SQL by hand.
# One schema and one login role per service; a schema's owner is the only role that can use it.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -v accounts_pw="$ACCOUNTS_DB_PASSWORD" \
  -v keycloak_pw="$KEYCLOAK_DB_PASSWORD" <<'SQL'
CREATE ROLE accounts LOGIN PASSWORD :'accounts_pw';
CREATE SCHEMA accounts AUTHORIZATION accounts;
ALTER ROLE accounts SET search_path = accounts;

CREATE ROLE keycloak LOGIN PASSWORD :'keycloak_pw';
CREATE SCHEMA keycloak AUTHORIZATION keycloak;
ALTER ROLE keycloak SET search_path = keycloak;
SQL
