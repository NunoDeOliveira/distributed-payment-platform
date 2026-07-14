#!/usr/bin/env bash

RESPONSE=$(curl -s -X POST "http://localhost:8080/payments" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"method":"INTERNATIONAL_TRANSFER"}') && \
echo "$RESPONSE" && \
PAYMENT_ID=$(python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])' <<< "$RESPONSE") && \
echo "Cancelling payment id=$PAYMENT_ID after 5 ms..." && \
sleep 0.005 && \
curl -i -X DELETE "http://localhost:8081/payments/$PAYMENT_ID"
