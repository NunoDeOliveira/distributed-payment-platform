#!/usr/bin/env bash

set -o xtrace
set -o pipefail

GATEWAY_URL="http://3.10.34.18:30000"

# Add balance to account
curl -sS -X POST "$GATEWAY_URL/balances/add?balanceAccount=500.00"
echo ""

# Pay 200.00 units. Successful payment
curl -sS -X POST "$GATEWAY_URL/payments" -H "Content-Type: application/json" -d '{"amount":200.00,"method":"INTERNATIONAL_TRANSFER"}'
echo ""

# Pay and cancel 100.00. Successful cancellation
RESPONSE=$(curl -sS -X POST "$GATEWAY_URL/payments" -H "Content-Type: application/json" -d '{"amount":100.00,"method":"INTERNATIONAL_TRANSFER"}') &&
echo "$RESPONSE" &&
PAYMENT_ID=$(python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])' <<< "$RESPONSE") &&
echo "Cancelling payment id=$PAYMENT_ID after 5 ms..." &&
sleep 0.005 &&
curl -i -X DELETE "$GATEWAY_URL/payments/$PAYMENT_ID"
echo ""

# Pay 300.00 units. Rejected payment
curl -sS -X POST "$GATEWAY_URL/payments" -H "Content-Type: application/json" -d '{"amount":300.00,"method":"INTERNATIONAL_TRANSFER"}'
