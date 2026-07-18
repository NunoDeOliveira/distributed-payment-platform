#!/usr/bin/env bash
GATEWAY_URL="${1:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

for i in $(seq 1 5); do
  echo "=== Run $i/5 ==="
  for j in 1 2 3; do
    echo "Launching test $j..."
    "$SCRIPT_DIR/test-pay-cancel-reject.sh" "$GATEWAY_URL" &
  done
  wait
  echo "=== Run $i/5 completed ==="
done

echo "=== All tests completed ==="
