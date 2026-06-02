#!/bin/bash
BASE_URL="http://localhost:8080"
BASE_URL_DIRECT="http://localhost:30081"

echo "=== Creando 5 producciones ==="
curl -s -X POST "$BASE_URL_DIRECT/productions?amount=10" & 
curl -s -X POST "$BASE_URL_DIRECT/productions?amount=12" &
curl -s -X POST "$BASE_URL_DIRECT/productions?amount=8" &
curl -s -X POST "$BASE_URL_DIRECT/productions?amount=15" &
curl -s -X POST "$BASE_URL_DIRECT/productions?amount=11" &
wait

echo ""
echo "=== Cancelando produccion 3 ==="
curl -s -X DELETE "$BASE_URL_DIRECT/productions/3"

sleep 2
echo ""
echo "=== Estado final producciones ==="
curl -s "$BASE_URL/production/productions"
