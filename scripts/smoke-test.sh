#!/usr/bin/env bash
# Runs every phase gate check so far against the Compose stack, starting it first if needed.
# Usage: scripts/smoke-test.sh   (Git Bash on Windows, or any bash with docker and curl)
# Each later phase adds its checks at the end. Exit code 0 means every check passed.
set -uo pipefail
cd "$(dirname "$0")/.."

[ -f .env ] || { echo "Missing .env: copy .env.example to .env and set the values"; exit 1; }
set -a; . ./.env; set +a

GATEWAY=${GATEWAY:-http://localhost:8080}
KEYCLOAK=${KEYCLOAK:-http://localhost:8180}
failures=0

check() { # description, expected, actual
    if [ "$2" = "$3" ]; then echo "PASS  $1"; else echo "FAIL  $1 (expected '$2', got '$3')"; failures=$((failures + 1)); fi
}
http_status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
token() { # username [client] -> access token from Keycloak (empty on failure)
    curl -s -d grant_type=password -d client_id="${2:-bank-cli}" -d username="$1" --data-urlencode "password=$DEMO_USER_PASSWORD" \
        "$KEYCLOAK/realms/bank/protocol/openid-connect/token" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

echo "Starting the stack (docker compose up -d --build --wait); the first run takes a few minutes..."
if ! docker compose up -d --build --wait >/dev/null; then
    echo "FAIL  stack did not become healthy"; docker compose ps; exit 1
fi

echo "Phase 1: foundation"
alice=$(token alice)
check "Keycloak issues a token for alice" yes "$([ -n "$alice" ] && echo yes || echo no)"
balance=$(curl -s -H "Authorization: Bearer $alice" "$GATEWAY/api/accounts/alice-current/balance")
check "GATE: balance of a seeded account through the gateway with a valid token" yes \
    "$(echo "$balance" | grep -qE '"amount":"-?[0-9]+\.[0-9]{2}","currency":"EUR"' && echo yes || echo no)"
check "request without a token is rejected" 401 "$(http_status "$GATEWAY/api/accounts")"
check "tampered token is rejected" 401 "$(http_status -H "Authorization: Bearer ${alice%.*}x.${alice##*.}" "$GATEWAY/api/accounts")"
check "forged X-User-Id is replaced by the token's user" "alice-current alice-savings" \
    "$(curl -s -H "Authorization: Bearer $alice" -H "X-User-Id: bob" "$GATEWAY/api/accounts" | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | xargs)"
check "another customer's account is not found" 404 \
    "$(http_status -H "Authorization: Bearer $alice" "$GATEWAY/api/accounts/bob-current/balance")"
# admin-cli is Keycloak's built-in client in every realm: a valid token, but not issued for bank-api
other=$(token alice admin-cli)
check "token for another client (wrong audience) is rejected" "yes 401" \
    "$([ -n "$other" ] && echo yes || echo no) $(http_status -H "Authorization: Bearer $other" "$GATEWAY/api/accounts")"

if [ "$failures" -eq 0 ]; then echo "All checks passed"; else echo "$failures check(s) failed"; fi
exit $((failures > 0))
