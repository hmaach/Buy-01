#!/usr/bin/env bash
# Checks the deployed stack the way a user reaches it:
#   1. the gateway reports UP
#   2. a real API call goes gateway -> Eureka -> product-service -> MongoDB
#   3. the frontend serves its index page
# Each check is retried, because services register in Eureka up to a minute after they are healthy.
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-https://127.0.0.1:8080}"
FRONTEND_URL="${FRONTEND_URL:-https://127.0.0.1:4200}"
TIMEOUT_SECONDS="${SMOKE_TIMEOUT:-180}"

# Build parameter SIMULATE_SMOKE_FAILURE: shows the automatic rollback during the audit.
if [ "${SMOKE_FORCE_FAIL:-false}" = "true" ]; then
  echo "SMOKE_FORCE_FAIL=true: failing on purpose to demonstrate the rollback"
  exit 1
fi

wait_for() {
  local name="$1"
  local url="$2"
  local waited=0
  # -k: both containers use self-signed certificates on localhost. Public TLS is done by host nginx.
  until curl -ksf -o /dev/null --max-time 10 "$url"; do
    if [ "$waited" -ge "$TIMEOUT_SECONDS" ]; then
      echo "FAIL $name ($url) after ${TIMEOUT_SECONDS}s"
      return 1
    fi
    sleep 10
    waited=$((waited + 10))
  done
  echo "OK   $name"
}

wait_for "gateway health" "$GATEWAY_URL/actuator/health"
wait_for "product API through the gateway" "$GATEWAY_URL/products"
wait_for "frontend" "$FRONTEND_URL/"
echo "Smoke test passed"
