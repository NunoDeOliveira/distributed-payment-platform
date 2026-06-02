#!/bin/bash
BASE_URL="http://localhost:8080"
BASE_URL_DIRECT="http://localhost:30081"

echo "=== Creando 10 producciones ==="
for i in $(seq 1 15); do
    curl -s -X POST "$BASE_URL_DIRECT/productions?amount=$((5 + RANDOM % 15))" &
done
wait

echo ""
echo "=== Cancelando producciones 2 5 8 ==="
curl -s -X DELETE "$BASE_URL_DIRECT/productions/2"
curl -s -X DELETE "$BASE_URL_DIRECT/productions/5"
curl -s -X DELETE "$BASE_URL_DIRECT/productions/8"

sleep 2
echo ""
echo "=== Estado final producciones ==="
curl -s "$BASE_URL/production/productions"
