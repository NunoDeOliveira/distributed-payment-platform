#!/bin/bash
BASE_URL="http://localhost:8080"

echo "=== Production 10 productions ==="
for i in $(seq 1 3); do
    curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))"
    echo ""
done


sleep 2
echo "=== Cancel latest delivery ==="
DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')
curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
echo ""


echo "=== Production 9 more productions ==="
for i in $(seq 1 2); do
    curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))"
    echo ""
done

sleep 5
echo "=== Final deliveries ==="
curl -s "$BASE_URL/delivery/deliveries"
echo ""
echo "=== Final productions ==="
curl -s "$BASE_URL/production/productions"
echo ""
